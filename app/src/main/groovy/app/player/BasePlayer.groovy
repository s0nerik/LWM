package app.player
import android.content.Context
import android.media.AudioManager
import android.support.annotation.NonNull
import app.Injector
import app.Utils
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.helper.DelayMeasurer
import app.model.Song
import com.google.android.exoplayer.ExoPlaybackException
import com.google.android.exoplayer.ExoPlayer
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer
import com.google.android.exoplayer.extractor.ExtractorSampleSource
import com.google.android.exoplayer.upstream.DefaultUriDataSource
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.subjects.PublishSubject

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static android.media.AudioManager.*
import static com.google.android.exoplayer.ExoPlayer.STATE_BUFFERING
import static com.google.android.exoplayer.ExoPlayer.STATE_ENDED
import static com.google.android.exoplayer.ExoPlayer.STATE_IDLE
import static com.google.android.exoplayer.ExoPlayer.STATE_PREPARING
import static com.google.android.exoplayer.ExoPlayer.STATE_READY

@CompileStatic
abstract class BasePlayer {

    int BUFFER_SEGMENT_SIZE = 1024
    int BUFFER_SEGMENT_COUNT = 512

    @Inject
    @PackageScope
    AudioManager audioManager

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    Context context

    boolean repeat = false
    boolean shuffle = false

    private ExoPlayer innerPlayer
    private MediaCodecAudioTrackRenderer renderer

    abstract void startService()

    private Song currentSong
    private Song lastSong

    protected DelayMeasurer prepareTimeMeasurer = new DelayMeasurer(10)

    private PublishSubject<Boolean> prepareSubject = PublishSubject.<Boolean>create()
    private PublishSubject<ExoPlaybackException> errorSubject = PublishSubject.<ExoPlaybackException>create()

    private int lastState = STATE_IDLE

    void onPlaybackEnded() { abandonAudioFocus() }
    private void onStartedBuffering() {}
    private void onBecameIdle() {
        abandonAudioFocus()
        stopNotifyingPlaybackProgress()
    }
    private void onStartedPreparing() {}
    private void onReady(boolean playWhenReady) {
        if (currentSong != lastSong) {
            lastSong = currentSong
            bus.post new SongChangedEvent(currentSong)
        }

        if (playWhenReady) {
            gainAudioFocus()
            bus.post new PlaybackStartedEvent(currentSong, innerPlayer.currentPosition)
            startNotifyingPlaybackProgress()
        }
    }
    void onError(ExoPlaybackException e) {
        Debug.e e
        abandonAudioFocus()
    }

    private ExoPlayer.Listener listener = [
            onPlayerStateChanged    : { boolean playWhenReady, int playbackState ->
                lastState = playbackState

                switch (playbackState) {
                    case STATE_ENDED:
                        onPlaybackEnded()
                        break
                    case STATE_IDLE:
                        onBecameIdle()
                        break
                    case STATE_PREPARING:
                        onStartedPreparing()
                        break
                    case STATE_BUFFERING:
                        onStartedBuffering()
                        break
                    case STATE_READY:
                        prepareSubject.onNext(playWhenReady)
                        prepareTimeMeasurer.stop()
                        onReady(playWhenReady)
                        break
                }
                Debug.d Utils.getConstantName(ExoPlayer, playbackState)
            },
            onPlayWhenReadyCommitted: {
                if (!innerPlayer.playWhenReady) {
                    bus.post new PlaybackPausedEvent(currentSong, innerPlayer.currentPosition)
                }
            },
            onPlayerError           : { ExoPlaybackException e ->
                if (lastState == STATE_PREPARING || lastState == STATE_BUFFERING) {
                    prepareSubject.onError(e)
                }
                onError e
            }
    ] as ExoPlayer.Listener

    static final int NOTIFY_INTERVAL = 1000

    private OnAudioFocusChangeListener afListener = { int focusChange ->
        switch (focusChange) {
            case AUDIOFOCUS_LOSS:
            case AUDIOFOCUS_LOSS_TRANSIENT:
            case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                innerPlayer.sendMessage renderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f
                break
            case AUDIOFOCUS_GAIN:
                innerPlayer.sendMessage renderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f
                break
        }
    } as OnAudioFocusChangeListener

    private Subscription playbackProgressNotifier

    BasePlayer() {
        Injector.inject this

        innerPlayer = ExoPlayer.Factory.newInstance 1, 10000, 20000
        innerPlayer.addListener listener

        startService()
    }

    String getCurrentPositionInMinutes() {
        int seconds = (currentPosition / 1000) as int
        int minutes = (seconds / 60) as int
        seconds -= minutes * 60
        return "${minutes}:${String.format('%02d', seconds)}"
    }

    void setPaused(boolean flag) {
        if (flag)
            pause()
        else
            start()
    }

    boolean isPaused() { !innerPlayer.playWhenReady }

    void togglePause() { paused = !paused }

    boolean isPlaying() { ready && !paused }

    boolean isReady() { innerPlayer.playbackState == STATE_READY }

    void start() {
        innerPlayer.playWhenReady = true
    }

    void pause() {
        innerPlayer.playWhenReady = false
    }

    void stop() {
        innerPlayer.stop()
//        seekTo 0
    }

    void seekTo(int msec) {
        innerPlayer.seekTo msec
    }

    int getCurrentPosition() { innerPlayer.currentPosition }

    void gainAudioFocus() { audioManager.requestAudioFocus afListener, STREAM_MUSIC, AUDIOFOCUS_GAIN }

    void abandonAudioFocus() { audioManager.abandonAudioFocus afListener }

    protected void startNotifyingPlaybackProgress() {
        playbackProgressNotifier = Observable.interval(NOTIFY_INTERVAL, TimeUnit.MILLISECONDS)
        .subscribe {
            if (currentSong) bus.post new SongPlayingEvent(currentPosition, currentSong.duration)
        }
    }

    protected void stopNotifyingPlaybackProgress() {
        playbackProgressNotifier?.unsubscribe()
        playbackProgressNotifier = null
    }

    Observable<Boolean> prepare(@NonNull Song song) {
        Observable.<Boolean>create({ Subscriber<Boolean> subscriber ->
            currentSong = song

            try {
                renderer = new MediaCodecAudioTrackRenderer(
                        new ExtractorSampleSource(currentSong.sourceUri,
                                new DefaultUriDataSource(context, "LWM"),
                                BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT
                        )
                )
                innerPlayer.prepare(renderer)

                prepareSubject.first().subscribe({
                    subscriber.onNext(it)
                    subscriber.onCompleted()
                }, {
                    subscriber.onError(it)
                    subscriber.onCompleted()
                })
            } catch (e) {
                subscriber.onError(e)
                subscriber.onCompleted()
            }
        } as Observable.OnSubscribe<Boolean>)
    }

    public Song getCurrentSong() { currentSong }

}
