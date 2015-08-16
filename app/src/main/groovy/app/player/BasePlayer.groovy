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
import com.google.android.exoplayer.extractor.Extractor
import com.google.android.exoplayer.extractor.ExtractorSampleSource
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer.extractor.ts.AdtsExtractor
import com.google.android.exoplayer.upstream.DefaultUriDataSource
import com.google.android.exoplayer.util.PlayerControl
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

    private PlayerControl playerControl

    protected ExoPlayer innerPlayer
    private MediaCodecAudioTrackRenderer renderer

    abstract void nextSong()
    abstract void prevSong()
    abstract void togglePause()
    abstract void startService()
    abstract boolean isShuffle()
    abstract boolean isRepeat()
    abstract Song getCurrentSong()

    void onPlaybackComplete() {}
    void onStartedBuffering() {}
    void onBecameIdle() {
        stopNotifyingPlaybackProgress()
    }
    void onStartedPreparing() {}
    void onReady(boolean playWhenReady) {
        if (playWhenReady) {
            bus.post new PlaybackStartedEvent(currentSong, currentPosition)
            startNotifyingPlaybackProgress()
            startService()
        }
    }
    void onError(ExoPlaybackException e) {}

    private ExoPlayer.Listener listener = [
            onPlayerStateChanged    : { boolean playWhenReady, int playbackState ->
                Debug.d playbackState as String
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        onPlaybackComplete()
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
        String event = ""
        switch (focusChange) {
            case AUDIOFOCUS_LOSS:
            case AUDIOFOCUS_LOSS_TRANSIENT:
            case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                event = "AUDIOFOCUS_LOSS || AUDIOFOCUS_LOSS_TRANSIENT || AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
                innerPlayer.sendMessage(renderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f)
                break
            case AUDIOFOCUS_GAIN:
                event = "AUDIOFOCUS_GAIN"
                innerPlayer.sendMessage(renderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f)
                break
        }
        Debug.d "onAudioFocusChange: ${event}"
    } as OnAudioFocusChangeListener

    private Timer playbackProgressNotifierTimer

    BasePlayer() {
        Injector.inject this

        innerPlayer = ExoPlayer.Factory.newInstance 1, 1000, 5000
        innerPlayer.addListener(listener)

        playerControl = new PlayerControl(innerPlayer)
    }

    boolean isReady() {
        return innerPlayer.playbackState == ExoPlayer.STATE_READY
    }

    String getCurrentPositionInMinutes() {
        int seconds = currentPosition / 1000 as int
        int minutes = seconds / 60 as int
        seconds -= minutes*60
        return "${minutes}:${String.format('%02d', seconds)}"
    }

    void pause() {
        innerPlayer.playWhenReady = false
        innerPlayer.stop()
        abandonAudioFocus()
    }

    void unpause() {
        gainAudioFocus()
        innerPlayer.playWhenReady = true
        prepareOld()
    }

    void stop() {
        innerPlayer.playWhenReady = false
        innerPlayer.stop()
        seekTo 0
        abandonAudioFocus()
    }

    void seekTo(int msec) {
        playerControl.seekTo msec
    }

    boolean isPlaying() {
        innerPlayer.playWhenReady
    }

    int getCurrentPosition() {
        playerControl.currentPosition
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

    private static Extractor getExtractor(String type) {
        switch (type?.toLowerCase()) {
            case "m4a": // There are no file format differences between M4A and MP4.
            case "mp4":
                return new Mp4Extractor()
            case "mp3":
                return new Mp3Extractor()
            case "aac":
                return new AdtsExtractor()
        }
        Debug.i "Unsupported type: ${type}"
        return null
    }

    private static String getExtension(String fileName) {
        def extStartIndex = fileName.lastIndexOf(".") + 1
        if (extStartIndex && extStartIndex < fileName.length() - 1)
            return fileName[extStartIndex..-1]
        else
            return null
    }

    boolean prepareNew(Uri uri) {
        def ext = getExtension uri.encodedSchemeSpecificPart
        def extractor = getExtractor(ext)
        if (ext && extractor) {
            renderer = new MediaCodecAudioTrackRenderer(
                    new ExtractorSampleSource(uri,
                            new DefaultUriDataSource(context, "LWM"),
                            extractor,
                            BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT
                    )
            )
            innerPlayer.prepare(renderer)
            return true
        } else {
            return false
        }
    }

    boolean prepareOld() {
        innerPlayer.prepare(renderer)
        return true
    }

}
