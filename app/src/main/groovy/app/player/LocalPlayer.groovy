package app.player
import android.content.Intent
import app.events.player.RepeatStateChangedEvent
import app.events.player.queue.*
import app.model.Song
import app.service.LocalPlayerService
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable

@CompileStatic
class LocalPlayer extends BasePlayer {

    private Queue queueContainer = new Queue()

    LocalPlayer() {
        bus.register this

        playerSubject.distinctUntilChanged()
                     .subscribe({
                                    switch (it) {
                                        case PlayerEvent.STARTED:
                                            break
                                        case PlayerEvent.PAUSED:
                                            break
                                        case PlayerEvent.ENDED:
                                            if (repeat)
                                                restart().subscribe()
                                            else
                                                prepareNextSong()
                                                        .concatWith(start())
                                                        .subscribe()
                                            break
                                        case PlayerEvent.IDLE:
                                            break
                                    }
                                }, {
                                    Debug.e "Error while playing:"
                                    Debug.e it
                                    stop().subscribe()
                                })
        
        errorSubject.subscribe {
            Debug.e "RxExoPlayer error:"
            Debug.e it
            stop().subscribe()
        }
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

    Observable prepare(int position) {
        Observable.concat(queueContainer.moveToAsObservable(position), prepare())
                  .ignoreElements()
    }

    Observable prepare() {
        Observable.concat(reset(), super.prepare())
                  .ignoreElements()
                  .doOnError {
            Debug.e currentSong.toString()
            Debug.e it

            queueContainer.moveToNext(true)
        }
                  .retry(3)
    }

    Observable prepareNextSong() {
        Observable.concat(queueContainer.moveToNextAsObservable(), prepare())
                  .ignoreElements()
    }

    Observable preparePrevSong() {
        Observable.concat(queueContainer.moveToPrevAsObservable(), prepare())
                  .ignoreElements()
    }

    @Override
    void startService() {
        context.startService new Intent(context, LocalPlayerService)
    }

    @Override
    Song getCurrentSong() {
        queueContainer.song
    }

    boolean isShuffle() { queueContainer.shuffled }

    void setRepeat(boolean flag) {
        this.@repeat = flag
        bus.post new RepeatStateChangedEvent(flag)
    }

    int getCurrentQueuePosition() { queueContainer.currentIndex }

    int getQueueSize() { queueContainer.size }

    boolean isSongInQueue(Song song) { queueContainer.contains song }

}