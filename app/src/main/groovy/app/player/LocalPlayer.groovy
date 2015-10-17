package app.player
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.events.player.ReadyToStartPlaybackEvent
import app.events.player.RepeatStateChangedEvent
import app.events.player.queue.*
import app.model.Song
import app.service.LocalPlayerService
import com.google.android.exoplayer.ExoPlaybackException
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class LocalPlayer extends BasePlayer {

    @Inject
    @PackageScope
    Context context

    @Inject
    @PackageScope
    Bus bus

    private Queue queueContainer = new Queue()

    @Override
    void onReady(boolean playWhenReady) {
        super.onReady(playWhenReady)

//        if (!playWhenReady)
//            bus.post new ReadyToStartPlaybackEvent(this, currentSong, currentPosition as int)
    }

    @Override
    void onPlaybackEnded() {
        super.onPlaybackEnded()

        if (repeat) {
            prepare()
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

    //region Queue manipulation
    void shuffleQueue() {
        queueContainer.shuffle()
        bus.post new QueueShuffledEvent(queue: queue)
    }

    List<Song> getQueue() { queueContainer.queue }

    void setQueue(List<Song> songs) {
        queueContainer.clear()
        queueContainer.addSongs songs
    }

    void shuffleQueueExceptPlayed() {
        queueContainer.shuffleExceptPlayed()
        bus.post(new QueueShuffledEvent(queue: queue))
    }

    void addToQueue(List<Song> songs) {
        queueContainer.addSongs(songs)
        bus.post(new PlaylistAddedToQueueEvent(queue, songs))
    }

    void addToQueue(Song song) {
        queueContainer.addSong(song)
        bus.post(new SongAddedToQueueEvent(queue, song))
    }

    void removeFromQueue(Song song) {
        queueContainer.removeSong(song)
        bus.post(new SongRemovedFromQueueEvent(queue, song))
    }

    void removeFromQueue(List<Song> songs) {
        queueContainer.removeSongs(songs)
        bus.post new PlaylistRemovedFromQueueEvent(queue, songs)
    }
    //endregion

    void prepare(int position) {
        queueContainer.moveTo position
        prepare()
    }

    void prepare() {
        pause()
        seekTo 0

        while (!prepare(queueContainer.song)) {
            queueContainer.moveToNext true
        }
    }

    void start() {
        paused = false
    }

    void nextSong() {
        if (queueContainer.moveToNext()) {
            prepare()
        } else {
            Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT).show()
        }
    }

    void prevSong() {
        if (queueContainer.moveToPrev()) {
            prepare()
        } else {
            Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT).show()
        }
    }

    @Override
    void startService() {
        context.startService new Intent(context, LocalPlayerService)
    }

    boolean isShuffle() { queueContainer.shuffled }

    void setRepeat(boolean flag) {
        repeat = flag
        bus.post new RepeatStateChangedEvent(flag)
    }

    int getCurrentQueuePosition() { queueContainer.currentIndex }

    int getQueueSize() { queueContainer.size }

    boolean isSongInQueue(Song song) { queueContainer.contains song }

}