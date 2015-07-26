package app.service
import android.app.Service
import android.content.Intent
import android.os.IBinder
import app.Injector
import app.websocket.WebSocketMessageClient
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@PackageScope(PackageScopeTarget.FIELDS)
@CompileStatic
class StreamPlayerService extends Service {

    @Inject
    Bus bus

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
}
