package app.player

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.queue.*
import app.events.server.PauseClientsEvent
import app.events.server.PrepareClientsEvent
import app.events.server.SeekToClientsEvent
import app.events.server.StartClientsEvent
import app.model.Song
import app.server.MusicServer
import app.service.LocalPlayerService
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
public class LocalPlayer extends BasePlayer {

    @Inject
    Bus bus;
    @Inject
    Context context;
    @Inject
    NotificationManager notificationManager;

    private boolean prepared = true;

    private boolean repeat = false;
    private boolean active = false;
    private Queue queue = new Queue();

    private MusicServer server;

    public LocalPlayer() {
        super();

        server = new MusicServer(this);

        onCompletionListener = {
            Log.d("LWM", "LocalPlayer: onCompletion");

            if (prepared) {
                int currentPosition = getCurrentPosition();
                int duration = getDuration() - 1000;

                if (currentPosition > 0 && duration > 0 && currentPosition > duration) {
                    if (isRepeat()) {
                        play();
                    } else {
                        nextSong();
                    }
                }
            }
        }
        onSeekCompleteListener = {
            start();
        }
        onPreparedListener = {
            prepared = true;
            active = true;

            bus.post(new SongChangedEvent(getCurrentSong()));

            if (server.isStarted()) {
                bus.post(new PrepareClientsEvent());
            } else {
                start();
            }
        }
        onErrorListener = { MediaPlayer mp, int what, int extra ->
            Debug.e("onError: " + what + ", " + extra)
            prepared = true
            return true
        }
    }

    public void shuffleQueue() {
        queue.shuffle();
        bus.post(new QueueShuffledEvent(getQueue()));
    }

    public List<Song> getQueue() {
        return queue.getQueue();
    }

    public void setQueue(List<Song> songs) {
        queue = new Queue(songs);
    }

    public void shuffleQueueExceptPlayed() {
        queue.shuffleExceptPlayed();
        bus.post(new QueueShuffledEvent(getQueue()));
    }

    public void addToQueue(List<Song> songs) {
        queue.addSongs(songs);
        bus.post(new PlaylistAddedToQueueEvent(getQueue(), songs));
    }

    public void addToQueue(Song song) {
        queue.addSong(song);
        bus.post(new SongAddedToQueueEvent(getQueue(), song));
    }

    public void removeFromQueue(Song song) {
        queue.removeSong(song);
        bus.post(new SongRemovedFromQueueEvent(getQueue(), song));
    }

    public void removeFromQueue(List<Song> songs) {
        queue.removeSongs(songs);
        bus.post(new PlaylistRemovedFromQueueEvent(getQueue(), songs));
    }

    public Song getCurrentSong() {
        return queue.getSong();
    }

    @Override
    public void stop() throws IllegalStateException {
        stopNotifyingPlaybackProgress();
        super.stop();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        Debug.d("LocalPlayer: seekTo(%s)", msec);
        if (prepared) {
            if (server.isStarted()) {
                bus.post(new SeekToClientsEvent(msec));
            }
            super.seekTo(msec);
        }
    }

    public void play(int position) {
        queue.moveTo(position);
        play();
    }

    public void play() {
        reset();
        try {
            assert queue != null : "queue == null";
            FileInputStream fis = new FileInputStream(queue.getSong().getSource());
            setDataSource(fis.getFD());
            prepareAsync();
            prepared = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCurrentSong() {
        return active && queue.getSong() != null;
    }

    @Override
    public void reset() {
        stopNotifyingPlaybackProgress();
        super.reset();
    }

    @Override
    public void nextSong() {
        Debug.d("LocalPlayer: nextSong");

        if (queue.moveToNext()) {
            play();
        } else {
            Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void prevSong() {
        Debug.d("LocalPlayer: prevSong");

        if (queue.moveToPrev()) {
            play();
        } else {
            Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void togglePause() {
        if (isPlaying()) {
            if (server.isStarted()) {
                bus.post(new PauseClientsEvent());
            } else {
                pause();
            }
        } else {
            if (server.isStarted()) {
                bus.post(new StartClientsEvent());
            } else {
                start();
            }
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (prepared) {
            stopNotifyingPlaybackProgress();
            super.pause();

            bus.post(new PlaybackPausedEvent(queue.getSong(), getCurrentPosition()));
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (prepared) {
            super.start();
            startNotifyingPlaybackProgress();

            bus.post(new PlaybackStartedEvent(queue.getSong(), getCurrentPosition()));
            context.startService(new Intent(context, LocalPlayerService.class));
        }
    }

//    @Override
//    public void reset() {
//        context.stopService(new Intent(context, LocalPlayerService.class));
//        super.reset();
//    }

    public boolean isShuffle() {
        return queue.isShuffled();
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean flag) {
        repeat = flag;
        bus.post(new RepeatStateChangedEvent(flag));
    }

    public void startServer() {
        server.start();
    }

    public void stopServer() {
        server.stop();
    }

    public MusicServer getServer() {
        return server;
    }

    public int getCurrentQueuePosition() {
        return queue.getCurrentIndex();
    }

    public int getQueueSize() {
        return queue.getSize();
    }

    public boolean isSongInQueue(Song song) {
        return queue.contains(song);
    }

}