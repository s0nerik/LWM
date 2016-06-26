package app.services

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import app.App
import app.commands.*
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.control.ControlButtonEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.players.LocalPlayer
import app.receivers.MediaButtonIntentReceiver
import com.github.s0nerik.rxbus.RxBus
import app.ui.notification.NowPlayingNotification
import app.websocket.WebSocketMessageServer
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.Observable
import rx.subscriptions.CompositeSubscription

import javax.inject.Inject

import static app.events.player.playback.control.ControlButtonEvent.Type.*

@CompileStatic
class LocalPlayerService extends Service {

    private CompositeSubscription sub = new CompositeSubscription()

    @Inject
    protected LocalPlayer player
    @Inject
    protected AudioManager audioManager

    @Inject
    protected WebSocketMessageServer server

    ComponentName mediaButtonsReceiver

    @Override
    void onCreate() {
        App.get().inject this
        initEventHandlers()
        player.server = server
        mediaButtonsReceiver = new ComponentName(packageName,
                                                 MediaButtonIntentReceiver.canonicalName)
        audioManager.registerMediaButtonEventReceiver mediaButtonsReceiver
    }

    @Override
    void onDestroy() {
        stopForeground true
        player.stop()
        sub.clear()
        audioManager.unregisterMediaButtonEventReceiver mediaButtonsReceiver
    }

    private void initEventHandlers() {
        RxBus.on(ChangePauseStateCommand).subscribe(this.&onEvent)
        RxBus.on(SeekToCommand).subscribe(this.&onEvent)
        RxBus.on(SetQueueAndPlayCommand).subscribe(this.&onEvent)
        RxBus.on(PlaySongAtPositionCommand).subscribe(this.&onEvent)
        RxBus.on(EnqueueCommand).subscribe(this.&onEvent)
        RxBus.on(ControlButtonEvent).subscribe(this.&onEvent)
        RxBus.on(PlaybackStartedEvent).subscribe(this.&onEvent)
        RxBus.on(PlaybackPausedEvent).subscribe(this.&onEvent)

        RxBus.post new CurrentSongAvailableEvent(player.currentSong)
    }

    @Override
    IBinder onBind(Intent intent) { null }

    @Override
    int onStartCommand(Intent intent, int flags, int startId) {
//        makeForeground player.playing
        return START_NOT_STICKY
    }

    private void makeForeground(boolean isPlaying) {
        startForeground 1337, new NowPlayingNotification(player.currentSong).create(isPlaying)
    }

    // region Event handlers

    private void onEvent(ChangePauseStateCommand cmd) {
        player.setPaused(cmd.pause).subscribe()
//        Observable observable
//
//        if (server.started)
//            if (cmd.pause) {
//                observable = Observable.merge(player.setPaused(cmd.pause), server.pauseClients())
//            } else {
//                observable = Observable.defer {
//                    server.prepareClients(new PrepareInfo(player.currentSong, System.currentTimeMillis(), player.currentPosition, false, false))
//                          .map { new ImmutablePair<Collection<WebSocket>, Long>(it, server.recommendedStartTime) }
//                          .doOnNext { bus.post new StartPlaybackDelayedCommand(it.right) }
//                          .concatMap { server.startClients(it.left, it.right) }
//                }
//            }
//        else
//            observable = player.setPaused(cmd.pause)
//
//        observable.subscribe { Debug.d "LocalPlayer setPaused: $it" }
    }

    private void onEvent(SeekToCommand cmd) {
        player.seekTo(cmd.position as int).subscribe {
            Debug.d "Player sought to: $it"
        }
    }

    private void onEvent(SetQueueAndPlayCommand cmd) {
        player.queue = cmd.queue
        if (cmd.shuffle) player.shuffleQueue()

        Observable.concat(player.prepare(cmd.position), player.start())
                  .subscribe { Debug.d "LocalPlayer prepared and started playback" }
    }

    private void onEvent(PlaySongAtPositionCommand cmd) {
        Observable<Object> prepare = null
        switch (cmd.positionType) {
            case PlaySongAtPositionCommand.PositionType.NEXT:
                prepare = player.prepareNextSong()
                break
            case PlaySongAtPositionCommand.PositionType.PREVIOUS:
                prepare = player.preparePrevSong()
                break
            case PlaySongAtPositionCommand.PositionType.EXACT:
                prepare = player.prepare(cmd.position)
                break
        }

        prepare.concatMap { player.start() }
               .subscribe()
    }

    private void onEvent(EnqueueCommand cmd) {
        player.addToQueue cmd.playlist
    }

//    @Subscribe
//    void onReadyToStartPlayback(ReadyToStartPlaybackEvent event) {
//        Debug.d()
//        if (serverStarted) {
//            bus.post new CurrentSongAvailableEvent(event.song)
//            bus.post new PrepareClientsCommand(event.position)
//        } else {
//            player.start().subscribe {
//                Debug.d "LocalPlayer started playback."
//            }
//        }
//    }

    private void onEvent(ControlButtonEvent event) {
        Debug.d event as String
        switch (event.type) {
            case NEXT:
                RxBus.post new PlaySongAtPositionCommand(PlaySongAtPositionCommand.PositionType.NEXT)
                break
            case PREV:
                RxBus.post new PlaySongAtPositionCommand(PlaySongAtPositionCommand.PositionType.PREVIOUS)
                break
            case PLAY:
            case PAUSE:
            case TOGGLE_PAUSE:
                player.togglePause().subscribe { Debug.d "LocalPlayer toggled pause" }
                break
        }
    }

    private void onEvent(PlaybackStartedEvent event) {
        makeForeground true
    }

    private void onEvent(PlaybackPausedEvent event) {
        makeForeground false
    }

    // endregion

}
