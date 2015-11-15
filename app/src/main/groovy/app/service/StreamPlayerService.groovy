package app.service
import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.Injector
import app.commands.StartPlaybackDelayedCommand
import app.events.player.ReadyToStartPlaybackEvent
import app.player.StreamPlayer
import app.websocket.WebSocketMessageClient
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug
import rx.Observable

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static app.websocket.SocketMessage.Message.READY
import static app.websocket.SocketMessage.Type.POST

@PackageScope(PackageScopeTarget.FIELDS)
@CompileStatic
class StreamPlayerService extends Service {

    @Inject
    Bus bus

    @Inject
    StreamPlayer player

    private WebSocketMessageClient webSocketMessageClient

    @Override
    void onCreate() {
        super.onCreate()
        Injector.inject this
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
        Observable.timer(cmd.startAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .subscribe {
                    player.setPaused(false).subscribe {
                        Debug.d "StreamPlayer started playback with delay."
                    }
                }
    }

}
