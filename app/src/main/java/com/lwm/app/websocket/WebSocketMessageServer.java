package com.lwm.app.websocket;

import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.Injector;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.events.server.ClientReadyEvent;
import com.lwm.app.model.chat.ChatMessage;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.websocket.entities.ClientInfo;
import com.squareup.otto.Bus;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class WebSocketMessageServer extends WebSocketServer {

    public static final String URI = "ws://192.168.43.1:8080";

    private static final int TIMEOUT = 10 * 1000; // 10 seconds

    private Set<WebSocket> ready;

    private Map<WebSocket, ClientInfo> clientInfoMap = new HashMap<>();

    private long lastMessageTime = -1;

    private LocalPlayer player;

    @Inject
    Bus bus;

    public WebSocketMessageServer(InetSocketAddress address, LocalPlayer player) {
        super(address);
        this.player = player;
        Injector.inject(this);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(App.TAG, "WebSocketMessageServer: New connection");
        Log.d(App.TAG, "WebSocketMessageServer: connections.size() = "+connections().size());
        conn.send(new SocketMessage(SocketMessage.Type.GET, SocketMessage.Message.CLIENT_INFO).toJson());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(App.TAG, "WebSocketMessageServer: Close connection");
        Log.d(App.TAG, "WebSocketMessageServer: connections.size() = "+connections().size());
        bus.post(new ClientDisconnectedEvent(clientInfoMap.get(conn)));
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(App.TAG, "WebSocket message: \"" + message + "\"");

        SocketMessage socketMessage = SocketMessage.fromJson(message);
        String body = socketMessage.getBody();

        if (socketMessage.getType() == SocketMessage.Type.GET) {
            switch (socketMessage.getMessage()) {
                case CURRENT_POSITION:
                    String pos = String.valueOf(player.getCurrentPosition());
                    conn.send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.CURRENT_POSITION, pos).toJson());
                    break;
                case IS_PLAYING:
                    String isPlaying = String.valueOf(player.isPlaying());
                    conn.send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.IS_PLAYING, isPlaying).toJson());
                    break;
                default:
                    Log.e(App.TAG, "Can't process message: "+socketMessage.getMessage().name());
            }
        } else if (socketMessage.getType() == SocketMessage.Type.POST) {
            switch (socketMessage.getMessage()) {
                case READY:
                    processReadiness(conn);
                    break;
                case CLIENT_INFO:
                    processClientInfo(conn, new Gson().fromJson(body, ClientInfo.class));
                    break;
                case MESSAGE:
                    ChatMessage chatMessage = new Gson().fromJson(body, ChatMessage.class);
                    bus.post(new ChatMessageReceivedEvent(chatMessage, conn));
                    break;
                default:
                    Log.e(App.TAG, "Can't process message: "+socketMessage.getMessage().name());
            }
        }

        lastMessageTime = System.currentTimeMillis();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(App.TAG,"WebSocketMessageServer onError:\n", ex);
    }

    private void processReadiness(WebSocket conn) {
        if (ready == null) {
            ready = new HashSet<>();
        }
        ready.add(conn);

        bus.post(new ClientReadyEvent(conn));

        if(ready.size() == connections().size()) {
            bus.post(new AllClientsReadyEvent());
            ready = null;
        }
    }

    private void processClientInfo(WebSocket conn, ClientInfo info) {
        clientInfoMap.put(conn, info);
        if (player.isPlaying()) {
            conn.send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.PREPARE).toJson());
        }
        bus.post(new ClientConnectedEvent(info));
    }

}
