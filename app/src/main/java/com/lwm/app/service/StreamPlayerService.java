package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lwm.app.Injector;
import com.lwm.app.events.server.ShouldStartWebSocketClientEvent;
import com.lwm.app.events.server.StopWebSocketClientEvent;
import com.lwm.app.helper.wifi.WifiUtils;
import com.lwm.app.websocket.WebSocketMessageClient;
import com.lwm.app.websocket.WebSocketMessageServer;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.net.URI;

import javax.inject.Inject;

public class StreamPlayerService extends Service {

    @Inject
    Bus bus;

    @Inject
    WifiUtils wifiUtils;

    private WebSocketMessageClient webSocketMessageClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        stopWebSocketClient(null);
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startWebSocketClient(null);
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe
    public void stopWebSocketClient(StopWebSocketClientEvent event) {
        if (webSocketMessageClient != null) {
            webSocketMessageClient.close();
            webSocketMessageClient = null;
        }
    }

    @Subscribe
    public void startWebSocketClient(ShouldStartWebSocketClientEvent event) {
        if (wifiUtils.isConnectedToStation() && webSocketMessageClient == null) {
            webSocketMessageClient = new WebSocketMessageClient(URI.create(WebSocketMessageServer.URI));
            webSocketMessageClient.connect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
