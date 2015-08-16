package app.player
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.events.player.RepeatStateChangedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.queue.*
import app.events.server.MusicServerStateChangedEvent
import app.events.server.PauseClientsEvent
import app.events.server.SeekToClientsEvent
import app.events.server.StartClientsEvent
import app.model.Song
import app.service.LocalPlayerService
import com.github.s0nerik.betterknife.annotations.Profile
import com.google.android.exoplayer.ExoPlaybackException
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
    private boolean repeat = false
    private Queue queue = new Queue()

    @Override
    void onPlaybackComplete() {
        Debug.d()

        if (isRepeat()) {
            play()
        } else {
            nextSong()
        }
    }

//    @Override
//    void onReady(boolean playWhenReady) {
//        bus.post new SongChangedEvent(currentSong)
//
//        if (serverStarted)
//            bus.post new PrepareClientsEvent()
//        else
//            start()
//    }

    @Override
    void onError(ExoPlaybackException e) {
        Debug.e e
    }

    LocalPlayer() {
        super()
        bus.register this

//        onSeekCompleteListener = {
//            start()
//        }
    }

    void shuffleQueue() {
        queue.shuffle()
        bus.post new QueueShuffledEvent(queue: getQueue())
    }

    List<Song> getQueue() {
        return queue.queue
    }

    void setQueue(List<Song> songs) {
        queue.clear()
        queue.addSongs songs
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
    void seekTo(int msec) {
        Debug.d "seekTo(${msec})"
        if (ready) {
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
        stop()
        innerPlayer.playWhenReady = true
        while (!prepareNew(Uri.parse("file://${queue.song?.source}"))) {
            queue.moveToNext true
        }
    }

    boolean hasCurrentSong() {
        return queue.song
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
        if (innerPlayer.playWhenReady) {
            if (serverStarted) {
                bus.post new PauseClientsEvent()
            } else {
                pause()
            }
        } else {
            if (serverStarted) {
                bus.post new StartClientsEvent()
            } else {
                unpause()
            }
        }
    }

    @Override
    void pause() {
        super.pause()

        bus.post new PlaybackPausedEvent(queue.song, currentPosition)
    }

    @Override
    void startService() {
        context.startService new Intent(context, LocalPlayerService)
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