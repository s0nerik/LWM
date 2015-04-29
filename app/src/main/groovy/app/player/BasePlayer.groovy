package app.player
import android.media.AudioManager
import android.media.MediaPlayer
import app.Injector
import app.events.player.playback.SongPlayingEvent
import app.model.Song
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
abstract class BasePlayer extends MediaPlayer {

    @Inject
    AudioManager audioManager;

    @Inject
    Bus bus;

    public abstract void nextSong();
    public abstract void prevSong();
    public abstract void togglePause();
    public abstract boolean isShuffle();
    public abstract boolean isRepeat();
    public abstract Song getCurrentSong();

    public static final int NOTIFY_INTERVAL = 1000;

    private AFListener afListener = new AFListener();

    private Timer playbackProgressNotifierTimer;

    public BasePlayer() {
        Injector.inject(this);
    }

    public String getCurrentPositionInMinutes(){
        int seconds = currentPosition / 1000 as int;
        int minutes = seconds / 60 as int;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d",seconds);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        audioManager.abandonAudioFocus(afListener);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        audioManager.requestAudioFocus(afListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    protected void startNotifyingPlaybackProgress() {
        playbackProgressNotifierTimer = new Timer();
        playbackProgressNotifierTimer.schedule(new PlaybackProgressNotifierTask(), 0, NOTIFY_INTERVAL);
    }

    protected void stopNotifyingPlaybackProgress() {
        if (playbackProgressNotifierTimer != null) {
            playbackProgressNotifierTimer.cancel();
            playbackProgressNotifierTimer = null;
        }
    }

    private class PlaybackProgressNotifierTask extends TimerTask {

        @Override
        public void run() {
            if (getCurrentSong() != null) bus.post(new SongPlayingEvent(getCurrentPosition(), getCurrentSong().getDuration()));
        }
    }

    private class AFListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            String event = "";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS || AUDIOFOCUS_LOSS_TRANSIENT || AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    BasePlayer.this.setVolume(0f, 0f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    BasePlayer.this.setVolume(1f, 1f);
                    break;
            }
            Debug.d("onAudioFocusChange: %s", event);
        }
    }
}
