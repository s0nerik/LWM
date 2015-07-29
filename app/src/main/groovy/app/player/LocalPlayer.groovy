package app.player

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongChangedEvent
import app.events.player.queue.*
import app.events.server.*
import app.model.Song
import app.service.LocalPlayerService
import com.github.s0nerik.betterknife.annotations.Profile
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug

import javax.inject.Inject

import static app.events.server.MusicServerStateChangedEvent.State.STARTED

@CompileStatic
class LocalPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Context context

    @Inject
    @PackageScope
    Bus bus

    private boolean serverStarted = false
    private boolean prepared = true
    private boolean repeat = false
    private boolean active = false
    private Queue queue = new Queue()

    LocalPlayer() {
        super()
        bus.register this

        onCompletionListener = {
            Debug.d()

            if (prepared) {
                int currentPosition = currentPosition
                int duration = duration - 1000

                if (currentPosition > 0 && duration > 0 && currentPosition > duration) {
                    if (isRepeat()) {
                        play()
                    } else {
                        nextSong()
                    }
                }
            }
        }
        onSeekCompleteListener = {
            start()
        }
        onPreparedListener = {
            prepared = true
            active = true

            bus.post new SongChangedEvent(currentSong)

            if (serverStarted) {
                bus.post new PrepareClientsEvent()
            } else {
                start()
            }
        }
        onErrorListener = { MediaPlayer mp, int what, int extra ->
            Debug.e "onError: $what, $extra"
            prepared = true
            return true
        }
    }

    void shuffleQueue() {
        queue.shuffle()
        bus.post new QueueShuffledEvent(queue: getQueue())
    }

    List<Song> getQueue() {
        return queue.queue
    }

    void setQueue(List<Song> songs) {
        queue = new Queue(songs)
    }

    void shuffleQueueExceptPlayed() {
        queue.shuffleExceptPlayed()
        bus.post(new QueueShuffledEvent(queue: getQueue()))
    }

    void addToQueue(List<Song> songs) {
        queue.addSongs(songs)
        bus.post(new PlaylistAddedToQueueEvent(getQueue(), songs))
    }

    void addToQueue(Song song) {
        queue.addSong(song)
        bus.post(new SongAddedToQueueEvent(getQueue(), song))
    }

    void removeFromQueue(Song song) {
        queue.removeSong(song)
        bus.post(new SongRemovedFromQueueEvent(getQueue(), song))
    }

    void removeFromQueue(List<Song> songs) {
        queue.removeSongs(songs)
        bus.post new PlaylistRemovedFromQueueEvent(getQueue(), songs)
    }

    Song getCurrentSong() {
        return queue.song
    }

    @Override
    void stop() throws IllegalStateException {
        stopNotifyingPlaybackProgress()
        super.stop()
    }

    @Override
    void seekTo(int msec) throws IllegalStateException {
        Debug.d "seekTo(${msec})"
        if (prepared) {
            if (serverStarted) {
                bus.post new SeekToClientsEvent(msec)
            }
            super.seekTo msec
        }
    }

    @Profile
    void play(int position) {
        queue.moveTo position
        play()
    }

    @Profile
//    @OnBackground
    void play() {
        reset()
        try {
            assert queue : "queue == null"
            FileInputStream fis = new FileInputStream(queue.song.source)
            setDataSource(fis.FD)
            prepareAsync()
            prepared = false
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    boolean hasCurrentSong() {
        return active && queue.song
    }

    @Override
    void reset() {
        stopNotifyingPlaybackProgress()
        super.reset()
    }

    @Override
    void nextSong() {
        Debug.d("LocalPlayer: nextSong")

        if (queue.moveToNext()) {
            play()
        } else {
            Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT).show()
        }

    }

    @Override
    void prevSong() {
        Debug.d("LocalPlayer: prevSong")

        if (queue.moveToPrev()) {
            play()
        } else {
            Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT).show()
        }

    }

    @Override
    void togglePause() {
        if (playing) {
            if (serverStarted) {
                bus.post new PauseClientsEvent()
            } else {
                pause()
            }
        } else {
            if (serverStarted) {
                bus.post new StartClientsEvent()
            } else {
                start()
            }
        }
    }

    @Override
    void pause() throws IllegalStateException {
        if (prepared) {
            stopNotifyingPlaybackProgress()
            super.pause()

            bus.post new PlaybackPausedEvent(queue.song, currentPosition)
        }
    }

    @Override
    void start() throws IllegalStateException {
        if (prepared) {
            super.start()
            startNotifyingPlaybackProgress()

            bus.post new PlaybackStartedEvent(queue.song, currentPosition)
            context.startService new Intent(context, LocalPlayerService)
        }
    }

//    @Override
//    void reset() {
//        context.stopService(new Intent(context, LocalPlayerService.class));
//        super.reset();
//    }

    boolean isShuffle() {
        return queue.shuffled
    }

    @Override
    boolean isRepeat() {
        return repeat
    }

    void setRepeat(boolean flag) {
        repeat = flag
        bus.post new RepeatStateChangedEvent(flag)
    }

    int getCurrentQueuePosition() {
        return queue.getCurrentIndex()
    }

    int getQueueSize() {
        return queue.getSize()
    }

    boolean isSongInQueue(Song song) {
        return queue.contains(song)
    }

    @Subscribe
    void onMusicServerStateChanged(MusicServerStateChangedEvent event) {
        serverStarted = event.state == STARTED
    }

}