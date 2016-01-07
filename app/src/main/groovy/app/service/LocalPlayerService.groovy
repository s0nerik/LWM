package app.service
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import app.Injector
import app.commands.*
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.control.ControlButtonEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.player.LocalPlayer
import app.receiver.MediaButtonIntentReceiver
import app.ui.notification.NowPlayingNotification
import app.websocket.WebSocketMessageServer
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.apache.commons.lang3.tuple.ImmutablePair
import org.java_websocket.WebSocket
import ru.noties.debug.Debug
import rx.Observable

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static app.events.player.playback.control.ControlButtonEvent.Type.*

@PackageScope(PackageScopeTarget.FIELDS)
@CompileStatic
class LocalPlayerService extends Service {

    @Inject
    Bus bus

    @Inject
    LocalPlayer player

    @Inject
    AudioManager audioManager

    @Inject
    WebSocketMessageServer server

    ComponentName mediaButtonsReceiver

    @Override
    void onCreate() {
        Injector.inject this

        bus.register this

        mediaButtonsReceiver = new ComponentName(packageName,
                                                 MediaButtonIntentReceiver.canonicalName)
        audioManager.registerMediaButtonEventReceiver mediaButtonsReceiver
    }

    @Override
    void onDestroy() {
        stopForeground true

        player.stop()

        bus.unregister this

        audioManager.unregisterMediaButtonEventReceiver mediaButtonsReceiver
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

    @Produce
    CurrentSongAvailableEvent produceCurrentSong() {
        new CurrentSongAvailableEvent(player.currentSong)
    }

    @Subscribe
    void startPlaybackDelayed(StartPlaybackDelayedCommand cmd) {
        Debug.d "cmd.startAt: $cmd.startAt"
        Debug.d "System.currentTimeMillis(): ${System.currentTimeMillis()}"
        def delay = cmd.startAt - System.currentTimeMillis()
        Debug.d "delay: $delay"

        player.start()
              .delaySubscription(delay, TimeUnit.MILLISECONDS)
              .doOnCompleted { Debug.d "LocalPlayer started playback with delay." }
              .subscribe()
    }

    @Subscribe
    void onPauseStateChanged(ChangePauseStateCommand cmd) {
        Observable observable

        if (server.started)
            if (cmd.pause) {
                observable = Observable.merge(player.setPaused(cmd.pause), server.pauseClients(server.connections()))
            } else {
                observable = Observable.defer {
                    server.prepareClients(player.currentPosition)
                          .map { new ImmutablePair<Collection<WebSocket>, Long>(it, server.recommendedStartTime) }
                          .doOnNext { bus.post new StartPlaybackDelayedCommand(it.right) }
                          .concatMap { server.startClients(it.left, it.right) }
                }
            }
        else
            observable = player.setPaused(cmd.pause)

        observable.subscribe { Debug.d "LocalPlayer setPaused: $it" }
    }

    @Subscribe
    void seekTo(SeekToCommand cmd) {
        player.seekTo(cmd.position as int).subscribe {
            Debug.d "Player sought to: $it"
        }
    }

    @Subscribe
    void setQueueAndPlayCommand(SetQueueAndPlayCommand cmd) {
        player.queue = cmd.queue
        if (cmd.shuffle) player.shuffleQueue()

        Observable.concat(player.prepare(cmd.position), player.start())
                  .subscribe { Debug.d "LocalPlayer prepared and started playback" }
    }

    @Subscribe
    void playSongAtPosition(PlaySongAtPositionCommand cmd) {
        Observable<Object> startPlayback
        if (server.started) {
            startPlayback = Observable.defer {
                server.prepareClients(player.currentPosition)
                      .map { new ImmutablePair<Collection<WebSocket>, Long>(it, server.recommendedStartTime) }
                      .doOnNext { bus.post new StartPlaybackDelayedCommand(it.right) }
                      .concatMap { server.startClients(it.left, it.right) }
            }
        } else {
            startPlayback = player.start()
        }

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

        prepare.concatWith(startPlayback)
               .subscribe()
    }

    @Subscribe
    void enqueue(EnqueueCommand cmd) {
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

    @Subscribe
    void onControlButton(ControlButtonEvent event) {
        Debug.d event as String
        switch (event.type) {
            case NEXT:
                bus.post new PlaySongAtPositionCommand(PlaySongAtPositionCommand.PositionType.NEXT)
                break
            case PREV:
                bus.post new PlaySongAtPositionCommand(PlaySongAtPositionCommand.PositionType.PREVIOUS)
                break
            case PLAY:
            case PAUSE:
            case TOGGLE_PAUSE:
                player.togglePause().subscribe { Debug.d "LocalPlayer toggled pause" }
                break
        }
    }

    @Subscribe
    void onPlaybackStarted(PlaybackStartedEvent event) {
        makeForeground true
    }

    @Subscribe
    void onPlaybackPaused(PlaybackPausedEvent event) {
        makeForeground false
    }

}
