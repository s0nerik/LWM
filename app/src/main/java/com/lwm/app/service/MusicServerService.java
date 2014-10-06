package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lwm.app.App;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.PrepareClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.server.StreamServer;
import com.lwm.app.websocket.SocketMessage;
import com.lwm.app.websocket.WebSocketMessageServer;
import com.squareup.otto.Subscribe;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MusicServerService extends Service {

    private WebSocketMessageServer webSocketMessageServer;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            new StreamServer(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                webSocketMessageServer = new WebSocketMessageServer(new InetSocketAddress(8080));
                webSocketMessageServer.start();
            }
        }).start();
        App.getBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getBus().unregister(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocketMessageServer.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Subscribe
    public void prepareClients(PrepareClientsEvent event) {
        sendAll(SocketMessage.PREPARE);
    }

    @Subscribe
    public void startClients(StartClientsEvent event) {
        sendAll(SocketMessage.START);
    }

    @Subscribe
    public void pauseClients(PauseClientsEvent event) {
        sendAll(SocketMessage.PAUSE);
    }

    private void sendAll(String message) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            conn.send(message);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
