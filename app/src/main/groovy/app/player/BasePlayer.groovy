package app.player
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import app.Injector
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.helper.DelayMeasurer
import app.model.Song
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer
import com.google.android.exoplayer.TrackRenderer
import com.google.android.exoplayer.extractor.ExtractorSampleSource
import com.google.android.exoplayer.upstream.DefaultUriDataSource
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscription

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static android.media.AudioManager.*
import static com.google.android.exoplayer.ExoPlayer.STATE_IDLE

@CompileStatic
abstract class BasePlayer extends RxExoPlayer {

    int BUFFER_SEGMENT_SIZE = 1024
    int BUFFER_SEGMENT_COUNT = 512

    @Inject
    @PackageScope
    protected AudioManager audioManager

    @Inject
    @PackageScope
    protected Bus bus

    @Inject
    @PackageScope
    protected Context context

    boolean repeat = false
    boolean shuffle = false

    abstract void startService()

    protected Song currentSong
    protected Song lastSong

    Song getCurrentSong() { currentSong }

    protected DelayMeasurer prepareTimeMeasurer = new DelayMeasurer(10)

    protected int lastState = STATE_IDLE

    static final int NOTIFY_INTERVAL = 1000

    private OnAudioFocusChangeListener afListener = { int focusChange ->
        switch (focusChange) {
            case AUDIOFOCUS_LOSS:
            case AUDIOFOCUS_LOSS_TRANSIENT:
            case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                innerPlayer.sendMessage currentRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f
                break
            case AUDIOFOCUS_GAIN:
                innerPlayer.sendMessage currentRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f
                break
        }
    } as OnAudioFocusChangeListener

    private Subscription playbackProgressNotifier

    BasePlayer() {
        Injector.inject this

        playerSubject.subscribe {
            //noinspection GroovyFallthrough
            switch (it) {
                case PlayerEvent.STARTED:
                    gainAudioFocus()
                    bus.post new PlaybackStartedEvent(currentSong, innerPlayer.currentPosition)
                    startNotifyingPlaybackProgress()
                    break
                case PlayerEvent.PAUSED:
                    bus.post new PlaybackPausedEvent(currentSong, innerPlayer.currentPosition)
                case PlayerEvent.ENDED:
                case PlayerEvent.IDLE:
                    abandonAudioFocus()
                    stopNotifyingPlaybackProgress()
                    break
            }
        }

        playerSubject.subscribe {
            if (currentSong != lastSong) {
                lastSong = currentSong
                bus.post new SongChangedEvent(currentSong)
            }
        }

        errorSubject.subscribe {
            Debug.e it
            abandonAudioFocus()
        }

        startService()
    }

    String getCurrentPositionInMinutes() {
        int seconds = (currentPosition / 1000) as int
        int minutes = (seconds / 60) as int
        seconds -= minutes * 60
        return "${minutes}:${String.format('%02d', seconds)}"
    }

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

    Observable<Boolean> prepare() {
        Observable.defer {
            prepare(currentSong.sourceUri)
        }
        .doOnNext {
            Debug.d "BasePlayer: prepare() onNext"
        }
        .doOnCompleted {
            Debug.d "BasePlayer: prepare() onCompleted"
        }
        .doOnSubscribe {
            Debug.d "BasePlayer: prepare() onSubscribe"
        }
    }

    @Override
    protected TrackRenderer getRenderer(Uri uri) {
        new MediaCodecAudioTrackRenderer(
                new ExtractorSampleSource(uri,
                        new DefaultUriDataSource(context, "LWM"),
                        BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT
                )
        )
    }
}
