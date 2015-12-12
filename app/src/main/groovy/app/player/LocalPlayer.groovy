package app.player

import android.content.Intent
import app.events.player.RepeatStateChangedEvent
import app.events.player.queue.*
import app.model.Song
import app.service.LocalPlayerService
import com.google.android.exoplayer.ExoPlayer
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable

@CompileStatic
class LocalPlayer extends BasePlayer {

    private Queue queueContainer = new Queue()

    LocalPlayer() {
        bus.register this

        playerSubject.distinct()
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
        queueContainer.moveToAsObservable(position)
                      .cast(Object)
                      .ignoreElements()
                      .concatWith(prepare())
    }

    Observable prepare() {
        Observable.defer {
            lastState != ExoPlayer.STATE_IDLE ? reset() : Observable.empty()
        }
                  .concatWith(super.prepare())
                  .doOnError {
            Debug.e "prepare() error"
            queueContainer.moveToNext true
        }
    }

    Observable prepareNextSong() {
        queueContainer.moveToNextAsObservable()
                      .cast(Object)
                      .ignoreElements()
                      .concatWith(prepare())
                      .doOnSubscribe {
            Debug.d "LocalPlayer: prepareNextSong() onSubscribe"
        }
                      .doOnNext {
            Debug.d "LocalPlayer: prepareNextSong() onNext"
        }
                      .doOnCompleted {
            Debug.d "LocalPlayer: prepareNextSong() onCompleted"
        }
    }

    Observable preparePrevSong() {
        queueContainer.moveToPrevAsObservable()
                      .cast(Object)
                      .ignoreElements()
                      .concatWith(prepare())
                      .doOnSubscribe {
            Debug.d "LocalPlayer: preparePrevSong() onSubscribe"
        }
                      .doOnNext {
            Debug.d "LocalPlayer: preparePrevSong() onNext"
        }
                      .doOnCompleted {
            Debug.d "LocalPlayer: preparePrevSong() onCompleted"
        }
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