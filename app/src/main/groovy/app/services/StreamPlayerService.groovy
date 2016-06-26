package app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.App
import app.commands.StartPlaybackDelayedCommand
import app.events.player.ReadyToStartPlaybackEvent
import app.players.StreamPlayer
import com.github.s0nerik.rxbus.RxBus
import app.websocket.WebSocketMessageClient
import groovy.transform.CompileStatic
import ru.noties.debug.Debug
import rx.subscriptions.CompositeSubscription

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static app.websocket.SocketMessage.Message.READY
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
class StreamPlayerService extends Service {

    private CompositeSubscription sub = new CompositeSubscription()

    @Inject
    protected StreamPlayer player

    private WebSocketMessageClient webSocketMessageClient

    @Override
    void onCreate() {
        super.onCreate()
        App.get().inject this
        initEventHandlers()
    }

    private void initEventHandlers() {
        RxBus.on(ReadyToStartPlaybackEvent).subscribe(this.&onEvent)
        RxBus.on(StartPlaybackDelayedCommand).subscribe(this.&onEvent)
    }

    @Override
    void onDestroy() {
        stopWebSocketClient()
        sub.clear()
        super.onDestroy()
    }

    @Override
    int onStartCommand(Intent intent, int flags, int startId) {
        startWebSocketClient intent.getStringExtra("uri")

        Debug.d "StreamPlayerService: ${intent.getStringExtra("uri")}"

        super.onStartCommand(intent, flags, startId)
    }

    @Override
    IBinder onBind(Intent intent) { null }

    private void stopWebSocketClient() {
        webSocketMessageClient?.close()
        webSocketMessageClient = null
    }

    private void startWebSocketClient(String uri) {
        webSocketMessageClient?.close()

        webSocketMessageClient = new WebSocketMessageClient(URI.create(uri))
        webSocketMessageClient.connect()
    }

    // region Event handlers

    private void onEvent(ReadyToStartPlaybackEvent event) {
        webSocketMessageClient.sendMessage POST, READY
    }

    private void onEvent(StartPlaybackDelayedCommand cmd) {
        Debug.d "cmd.startAt: $cmd.startAt"
        Debug.d "System.currentTimeMillis(): ${System.currentTimeMillis()}"
        def delay = cmd.startAt - System.currentTimeMillis()
        Debug.d "delay: $delay"

        player.start()
              .delaySubscription(delay, TimeUnit.MILLISECONDS)
              .doOnCompleted { Debug.d "StreamPlayer started playback with delay." }
              .subscribe()
    }

    // endregion

}
