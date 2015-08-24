package app.player
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import app.Injector
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongPlayingEvent
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

import javax.inject.Inject

import static android.media.AudioManager.*
/**
 * Stop means pause.
 * To start any song from the beginning the "seekTo(0)" should be called.
 */
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

    boolean playing = false
    boolean repeat = false
    boolean shuffle = false
    Song currentSong

    Uri playbackUri

    protected ExoPlayer innerPlayer
    protected MediaCodecAudioTrackRenderer renderer

    abstract void nextSong()
    abstract void prevSong()
    abstract void togglePause()
    abstract void startService()

    void onPlaybackEnded() {
        Debug.d()
        playing = false
        abandonAudioFocus()
    }
    void onStartedBuffering() {
        Debug.d()
    }
    void onBecameIdle() {
        Debug.d()
        stopNotifyingPlaybackProgress()
    }
    void onStartedPreparing() {
        Debug.d()
    }
    void onReady(boolean playWhenReady) {
        if (playWhenReady) {
            gainAudioFocus()
            bus.post new PlaybackStartedEvent(currentSong, currentPosition)
            startNotifyingPlaybackProgress()
            startService()
            playing = true
        }
    }
    void onError(ExoPlaybackException e) {
        Debug.e e
        abandonAudioFocus()
    }

    private ExoPlayer.Listener listener = [
            onPlayerStateChanged    : { boolean playWhenReady, int playbackState ->
                Debug.d playbackState as String
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        onPlaybackEnded()
                        break
                    case ExoPlayer.STATE_BUFFERING:
                        onStartedBuffering()
                        break
                    case ExoPlayer.STATE_IDLE:
                        onBecameIdle()
                        break
                    case ExoPlayer.STATE_PREPARING:
                        onStartedPreparing()
                        break
                    case ExoPlayer.STATE_READY:
                        onReady(playWhenReady)
                        break
                }
            },
            onPlayWhenReadyCommitted: {},
            onPlayerError           : { ExoPlaybackException e -> onError e }
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

    private Timer playbackProgressNotifierTimer

    BasePlayer() {
        Injector.inject this

        innerPlayer = ExoPlayer.Factory.newInstance 1, 1000, 5000
        innerPlayer.addListener(listener)
    }

    String getCurrentPositionInMinutes() {
        int seconds = (currentPosition / 1000) as int
        int minutes = (seconds / 60) as int
        seconds -= minutes * 60
        return "${minutes}:${String.format('%02d', seconds)}"
    }

    void pause() {
        innerPlayer.playWhenReady = false
    }

    void unpause() {
        innerPlayer.playWhenReady = true
    }

    void stop() {
        innerPlayer.playWhenReady = false
        innerPlayer.stop()
        seekTo 0
    }

    void seekTo(int msec) {
        innerPlayer.seekTo msec
    }

    int getCurrentPosition() {
        innerPlayer.currentPosition
    }

    void gainAudioFocus() {
        audioManager.requestAudioFocus afListener, STREAM_MUSIC, AUDIOFOCUS_GAIN
    }

    void abandonAudioFocus() {
        audioManager.abandonAudioFocus afListener
    }

    protected void startNotifyingPlaybackProgress() {
        playbackProgressNotifierTimer = new Timer()
        playbackProgressNotifierTimer.schedule(new TimerTask() {
            @Override
            void run() {
                if (getCurrentSong()) bus.post new SongPlayingEvent(getCurrentPosition(), getCurrentSong().duration)
            }
        }, 0, NOTIFY_INTERVAL)
    }

    protected void stopNotifyingPlaybackProgress() {
        playbackProgressNotifierTimer?.cancel()
        playbackProgressNotifierTimer = null
    }

    private boolean prepareInternal(Uri uri) {
        try {
            renderer = new MediaCodecAudioTrackRenderer(
                    new ExtractorSampleSource(uri,
                            new DefaultUriDataSource(context, "LWM"),
                            BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT
                    )
            )
            innerPlayer.prepare(renderer)
            playbackUri = uri
            return true
        } catch (IllegalStateException e) {
            return false
        }
    }

    private boolean prepareOld() {
        innerPlayer.prepare(renderer)
        return true
    }

    boolean prepare(Uri uri, boolean prepareAgain = false) {
        return uri == playbackUri && !prepareAgain ? prepareOld() : prepareInternal(uri)
    }

}
