package app.player

import android.content.Intent
import app.events.player.RepeatStateChangedEvent
import app.events.player.queue.*
import app.model.Song
import app.service.LocalPlayerService
import app.websocket.WebSocketMessageServer
import app.websocket.entities.PrepareInfo
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable

import javax.inject.Inject

@CompileStatic
class LocalPlayer extends BasePlayer {

    @Inject
    @PackageScope
    WebSocketMessageServer server

    private Queue queueContainer = new Queue()

    LocalPlayer() {
        bus.register this

        def playerEvents = playerSubject.distinctUntilChanged()

        playerEvents.filter { it == PlayerEvent.ENDED }
                    .concatMap { repeat ? restart() : prepareNextSong().concatWith(start()) }
                    .subscribe()

        playerEvents.doOnError {
            Debug.e "Error while playing:"
            Debug.e it
            stop().subscribe()
        }.subscribe()

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

    Observable prepare(Song song) {
        def observable = reset().concatMap { super.prepare(song) }
                                .doOnError { Debug.e currentSong.toString() }
                                .onErrorResumeNext(queueContainer.moveToNextAsObservable(true)
                                                                 .concatMap { prepare it })
        if (server.started) {
            observable = observable.concatMap { server.prepareClients(new PrepareInfo(song)) }
        }
        return observable
    }

    @Override
    Observable start() {
        if (server.started)
            return server.startClients()
                         .concatMap { super.start() }

        return super.start()
    }

    Observable prepare(int position) {
        queueContainer.moveToAsObservable(position)
                      .concatMap { prepare(it) }
    }

    Observable prepareNextSong() {
        queueContainer.moveToNextAsObservable()
                      .concatMap { prepare(it) }
    }

    Observable preparePrevSong() {
        queueContainer.moveToPrevAsObservable()
                      .concatMap { prepare(it) }
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