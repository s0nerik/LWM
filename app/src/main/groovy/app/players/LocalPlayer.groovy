package app.players

import android.content.Intent
import app.App
import app.events.player.RepeatStateChangedEvent
import app.events.player.queue.*
import app.models.Song
import com.github.s0nerik.rxbus.RxBus
import app.services.LocalPlayerService
import app.websocket.WebSocketMessageServer
import app.websocket.entities.PrepareInfo
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable

@CompileStatic
class LocalPlayer extends BasePlayer {

    WebSocketMessageServer server

    private Queue queueContainer = new Queue()

    LocalPlayer() {
        def playerEvents = playerSubject.distinctUntilChanged()

        playerEvents.filter { it == PlayerEvent.ENDED }
                    .concatMap { repeat ? restart() : prepareNextSong().concatWith(start()) }
                    .subscribe()

        playerEvents.doOnError {
            Debug.e "Error while playing:"
            Debug.e it
            stop().subscribe()
        }.subscribe()

//        errorSubject.subscribe {
//            Debug.e "RxExoPlayer error:"
//            Debug.e it
//            stop().subscribe()
//        }
    }

    @Override
    protected void injectDependencies() {
        App.get().inject(this)
    }

    //region Queue manipulation
    void shuffleQueue() {
        queueContainer.shuffle()
        RxBus.post new QueueShuffledEvent(queue: queue)
    }

    List<Song> getQueue() { queueContainer.queue }

    void setQueue(List<Song> songs) {
        queueContainer.clear()
        queueContainer.addSongs songs
    }

    void shuffleQueueExceptPlayed() {
        queueContainer.shuffleExceptPlayed()
        RxBus.post(new QueueShuffledEvent(queue: queue))
    }

    void addToQueue(List<Song> songs) {
        queueContainer.addSongs(songs)
        RxBus.post(new PlaylistAddedToQueueEvent(queue, songs))
    }

    void addToQueue(Song song) {
        queueContainer.addSong(song)
        RxBus.post(new SongAddedToQueueEvent(queue, song))
    }

    void removeFromQueue(Song song) {
        queueContainer.removeSong(song)
        RxBus.post(new SongRemovedFromQueueEvent(queue, song))
    }

    void removeFromQueue(List<Song> songs) {
        queueContainer.removeSongs(songs)
        RxBus.post new PlaylistRemovedFromQueueEvent(queue, songs)
    }
    //endregion

    Observable prepare(Song song, int retriesLeft = 3) {
        def observable = reset().concatMap { super.prepare(song) }
                                .doOnError { Debug.e currentSong.toString() }

        if (retriesLeft > 0)
            observable = observable.onErrorResumeNext(
                    queueContainer.moveToNextAsObservable(true)
                                  .concatMap { prepare it, retriesLeft - 1 }
            )

        if (server?.started)
            observable = observable.concatMap { server.prepareClients(new PrepareInfo(song)) }

        return observable
    }

    @Override
    Observable start() {
        if (server?.started)
            return server.startClients()
                         .concatMap { super.start() }

        return super.start()
    }

    @Override
    Observable pause() {
        if (server?.started)
            return super.pause()
                        .concatMap { server.pauseClients() }

        return super.pause()
    }

    @Override
    Observable seekTo(int msec) {
        if (server?.started)
            return super.pause()
                        .concatMap { super.seekTo(msec) }
                        .concatMap { server.prepareClients(PrepareInfo.builder()
                                                                      .song(currentSong)
                                                                      .seeking(true)
                                                                      .position(msec)
                                                                      .build()) }
                        .concatMap { start() }

        return super.seekTo(msec)
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
        RxBus.post new RepeatStateChangedEvent(flag)
    }

    int getCurrentQueuePosition() { queueContainer.currentIndex }

    int getQueueSize() { queueContainer.size }

    boolean isSongInQueue(Song song) { queueContainer.contains song }

}