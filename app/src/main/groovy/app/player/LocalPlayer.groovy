package app.player
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.events.player.RepeatStateChangedEvent
import app.events.player.queue.*
import app.events.player.service.CurrentSongAvailableEvent
import app.events.server.MusicServerStateChangedEvent
import app.events.server.PrepareClientsEvent
import app.model.Song
import app.service.LocalPlayerService
import com.github.s0nerik.betterknife.annotations.Profile
import com.google.android.exoplayer.ExoPlaybackException
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

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
    private Queue queue = new Queue()

    @Override
    void onPlaybackEnded() {
        super.onPlaybackEnded()

        if (repeat) {
            play()
        } else {
            nextSong()
        }
    }

    @Override
    void onError(ExoPlaybackException e) {
        super.onError(e)
        nextSong()
    }

    LocalPlayer() {
        super()
        bus.register this
    }

    void shuffleQueue() {
        queue.shuffle()
        bus.post new QueueShuffledEvent(queue: getQueue())
    }

    List<Song> getQueue() { queue.queue }

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

    @Override
    Song getCurrentSong() {
        return queue.song
    }

    void play(int position) {
        queue.moveTo position
        play()
    }

    void play() {
        stop()

        while (!prepare(queue.song?.sourceUri)) {
            queue.moveToNext true
        }

        if (serverStarted) {
            bus.post new CurrentSongAvailableEvent(currentSong)
            bus.post new PrepareClientsEvent(0)
        } else {
            paused = false
        }
    }

    @Override
    void nextSong() {
        if (queue.moveToNext()) {
            play()
        } else {
            Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT).show()
        }
    }

    @Override
    void prevSong() {
        if (queue.moveToPrev()) {
            play()
        } else {
            Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT).show()
        }
    }

    @Override
    void startService() {
        context.startService new Intent(context, LocalPlayerService)
    }

    boolean isShuffle() { queue.shuffled }

    void setRepeat(boolean flag) {
        repeat = flag
        bus.post new RepeatStateChangedEvent(flag)
    }

    int getCurrentQueuePosition() { queue.currentIndex }

    int getQueueSize() { queue.size }

    boolean isSongInQueue(Song song) { queue.contains song }

    @Subscribe
    void onMusicServerStateChanged(MusicServerStateChangedEvent event) {
        serverStarted = event.state == STARTED
    }

}