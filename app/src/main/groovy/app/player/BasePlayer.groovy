package app.player
import android.media.AudioManager
import android.media.MediaPlayer
import app.Injector
import app.events.player.playback.SongPlayingEvent
import app.model.Song
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

import static android.media.AudioManager.*

@CompileStatic
abstract class BasePlayer extends MediaPlayer {

    @Inject
    @PackageScope
    AudioManager audioManager

    @Inject
    @PackageScope
    Bus bus

    abstract void nextSong()
    abstract void prevSong()
    abstract void togglePause()
    abstract boolean isShuffle()
    abstract boolean isRepeat()
    abstract Song getCurrentSong()

    static final int NOTIFY_INTERVAL = 1000

    private OnAudioFocusChangeListener afListener = { int focusChange ->
        String event = ""
        switch (focusChange) {
            case AUDIOFOCUS_LOSS:
            case AUDIOFOCUS_LOSS_TRANSIENT:
            case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                event = "AUDIOFOCUS_LOSS || AUDIOFOCUS_LOSS_TRANSIENT || AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
                setVolume 0f, 0f
                break
            case AUDIOFOCUS_GAIN:
                event = "AUDIOFOCUS_GAIN"
                setVolume 1f, 1f
                break
        }
        Debug.d "onAudioFocusChange: ${event}"
    } as OnAudioFocusChangeListener

    private Timer playbackProgressNotifierTimer

    BasePlayer() {
        Injector.inject this
    }

    String getCurrentPositionInMinutes() {
        int seconds = getCurrentPosition() / 1000 as int
        int minutes = seconds / 60 as int
        seconds -= minutes*60
        return "${minutes}:${String.format('%02d', seconds)}"
    }

    @Override
    void pause() throws IllegalStateException {
        super.pause()
        audioManager.abandonAudioFocus afListener
    }

    @Override
    void start() throws IllegalStateException {
        super.start()
        audioManager.requestAudioFocus afListener, STREAM_MUSIC, AUDIOFOCUS_GAIN
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

}
