package app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.App
import app.commands.StartPlaybackDelayedCommand
import app.events.player.ReadyToStartPlaybackEvent
import app.players.StreamPlayer
import app.websocket.WebSocketMessageClient
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static app.websocket.SocketMessage.Message.READY
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
class StreamPlayerService extends Service {

    @Inject
    protected Bus bus

    @Inject
    protected StreamPlayer player

    private WebSocketMessageClient webSocketMessageClient

    @Override
    void onCreate() {
        super.onCreate()
        App.get().inject this
        bus.register this
    }

    @Override
    void onDestroy() {
        stopWebSocketClient()
        bus.unregister this
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

    @Subscribe
    void onReadyToStartPlayback(ReadyToStartPlaybackEvent event) {
        webSocketMessageClient.sendMessage POST, READY
    }

    @Subscribe
    void startPlaybackDelayed(StartPlaybackDelayedCommand cmd) {
        Debug.d "cmd.startAt: $cmd.startAt"
        Debug.d "System.currentTimeMillis(): ${System.currentTimeMillis()}"
        def delay = cmd.startAt - System.currentTimeMillis()
        Debug.d "delay: $delay"

        player.start()
              .delaySubscription(delay, TimeUnit.MILLISECONDS)
              .doOnCompleted { Debug.d "StreamPlayer started playback with delay." }
              .subscribe()
    }

}
