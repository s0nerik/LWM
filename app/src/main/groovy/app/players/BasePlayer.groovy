package app.players

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.playback.SongPlayingEvent
import app.helpers.DelayMeasurer
import app.models.Song
import app.rx.RxBus
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer
import com.google.android.exoplayer.MediaCodecSelector
import com.google.android.exoplayer.TrackRenderer
import com.google.android.exoplayer.extractor.ExtractorSampleSource
import com.google.android.exoplayer.upstream.DefaultAllocator
import com.google.android.exoplayer.upstream.DefaultUriDataSource
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscription

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static android.media.AudioManager.*
import static com.google.android.exoplayer.ExoPlayer.STATE_IDLE

@CompileStatic
abstract class BasePlayer extends RxExoPlayer {

    @Inject
    protected AudioManager audioManager

    @Inject
    protected Context context

    public boolean repeat = false
    boolean shuffle = false

    abstract void startService()

    Song currentSong
    protected Song lastSong

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
        injectDependencies()

        playerSubject.subscribe {
            //noinspection GroovyFallthrough
            switch (it) {
                case PlayerEvent.STARTED:
                    gainAudioFocus()
                    RxBus.post new PlaybackStartedEvent(currentSong, innerPlayer.currentPosition)
                    startNotifyingPlaybackProgress()
                    break
                case PlayerEvent.PAUSED:
                    RxBus.post new PlaybackPausedEvent(currentSong, innerPlayer.currentPosition)
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
                RxBus.post new SongChangedEvent(currentSong)
            }
        }

        errorSubject.subscribe {
            Debug.e it
            abandonAudioFocus()
        }

        startService()
    }

    protected abstract void injectDependencies()

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
                                                 if (currentSong) RxBus.post new SongPlayingEvent(currentPosition, currentSong.duration)
                                             }
    }

    protected void stopNotifyingPlaybackProgress() {
        playbackProgressNotifier?.unsubscribe()
        playbackProgressNotifier = null
    }

    Observable prepare(Song song) {
        Observable.defer {
            prepare(song.sourceUri).doOnSubscribe { currentSong = song }
        }
    }

    @Override
    protected TrackRenderer getRenderer(Uri uri) {
        def allocator = new DefaultAllocator(bufferSegmentSize);
        new MediaCodecAudioTrackRenderer(
                new ExtractorSampleSource(uri,
                                          new DefaultUriDataSource(context, "LWM"),
                                          allocator,
                                          bufferSegmentSize * bufferSegmentCount
                ), MediaCodecSelector.DEFAULT
        )
    }

    protected int getBufferSegmentSize() { 64 * 1024 }

    protected int getBufferSegmentCount() { 256 }
}
