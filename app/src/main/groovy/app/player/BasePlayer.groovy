package app.player
import android.content.Context
import android.media.AudioManager
import android.net.Uri
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

import javax.inject.Inject

import static android.media.AudioManager.*

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

    Uri playbackUri

    protected ExoPlayer innerPlayer
    protected MediaCodecAudioTrackRenderer renderer

    abstract void nextSong()
    abstract void prevSong()
    abstract void startService()
    abstract Song getCurrentSong()

    private Song lastSong

    protected DelayMeasurer prepareTimeMeasurer = new DelayMeasurer()

    void onPlaybackEnded() { abandonAudioFocus() }
    void onStartedBuffering() {}
    void onBecameIdle() {
        abandonAudioFocus()
        stopNotifyingPlaybackProgress()
    }
    void onStartedPreparing() {}
    void onReady(boolean playWhenReady) {
        if (getCurrentSong() != lastSong) {
            lastSong = getCurrentSong()
            bus.post new SongChangedEvent(getCurrentSong())
        }

        if (playWhenReady) {
            gainAudioFocus()
            bus.post new PlaybackStartedEvent(getCurrentSong(), currentPosition)
            startNotifyingPlaybackProgress()
            startService()
        }
    }
    void onError(ExoPlaybackException e) {
        Debug.e e
        abandonAudioFocus()
    }

    private ExoPlayer.Listener listener = [
            onPlayerStateChanged    : { boolean playWhenReady, int playbackState ->
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
                        prepareTimeMeasurer.stop()
                        onReady(playWhenReady)
                        break
                }
                Debug.d Utils.getConstantName(ExoPlayer, playbackState)
            },
            onPlayWhenReadyCommitted: {
                if (!innerPlayer.playWhenReady) {
                    bus.post new PlaybackPausedEvent(getCurrentSong(), currentPosition)
                }
            },
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

        innerPlayer = ExoPlayer.Factory.newInstance 1, 10000, 20000
        innerPlayer.addListener listener

        paused = true
    }

    String getCurrentPositionInMinutes() {
        int seconds = (currentPosition / 1000) as int
        int minutes = (seconds / 60) as int
        seconds -= minutes * 60
        return "${minutes}:${String.format('%02d', seconds)}"
    }

    void setPaused(boolean flag) {
        innerPlayer.playWhenReady = !flag
    }

    boolean isPaused() { !innerPlayer.playWhenReady }

    void togglePause() { paused = !paused }

    boolean isPlaying() { !paused && ready }

    boolean isReady() { innerPlayer.playbackState == ExoPlayer.STATE_READY }

    void stop() {
        innerPlayer.stop()
        seekTo 0
    }

    void seekTo(int msec) {
        innerPlayer.seekTo msec
    }

    int getCurrentPosition() { innerPlayer.currentPosition }

    void gainAudioFocus() { audioManager.requestAudioFocus afListener, STREAM_MUSIC, AUDIOFOCUS_GAIN }

    void abandonAudioFocus() { audioManager.abandonAudioFocus afListener }

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

    boolean prepare(Uri uri, boolean reprepare = false) {
        if (uri == playbackUri && !reprepare) {
            prepareOld()
        } else {
            prepareTimeMeasurer.start()
//            innerPlayer.stop()
            prepareInternal uri
        }
    }

}
