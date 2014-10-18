package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.ChatMessagesAvailableEvent;
import com.lwm.app.events.chat.NotifyMessageAddedEvent;
import com.lwm.app.events.chat.SendChatMessageEvent;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.PrepareClientsEvent;
import com.lwm.app.events.server.SeekToClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.model.chat.ChatMessage;
import com.lwm.app.server.StreamServer;
import com.lwm.app.websocket.SocketMessage;
import com.lwm.app.websocket.WebSocketMessageServer;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MusicServerService extends Service {

    private WebSocketMessageServer webSocketMessageServer;
    private LocalPlayerService player;

    private List<ChatMessage> chatMessages = new ArrayList<>();

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
        player = App.getLocalPlayerService();
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
        if (webSocketMessageServer.connections().size() != 0) {
            sendAll(SocketMessage.getStringToSend(SocketMessage.PREPARE));
        } else {
            App.getBus().post(new AllClientsReadyEvent());
        }
    }

    @Subscribe
    public void allClientsReady(AllClientsReadyEvent event) {
        sendAll(SocketMessage.formatWithInt(SocketMessage.START_FROM, player.getCurrentPosition()));
    }

    @Subscribe
    public void startClients(StartClientsEvent event) {
        sendAll(SocketMessage.getStringToSend(SocketMessage.START));
    }

    @Subscribe
    public void pauseClients(PauseClientsEvent event) {
        sendAll(SocketMessage.getStringToSend(SocketMessage.PAUSE));
    }

    @Subscribe
    public void seekToClients(SeekToClientsEvent event) {
        sendAll(SocketMessage.formatWithInt(SocketMessage.SEEK_TO, event.getPosition()));
    }

    @Produce
    public ChatMessagesAvailableEvent produceMessages() {
        return new ChatMessagesAvailableEvent(chatMessages);
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        ChatMessage msg = event.getMessage();
        chatMessages.add(msg);
        sendAllExcept(SocketMessage.formatWithString(SocketMessage.MESSAGE, new Gson().toJson(msg)), event.getWebSocket());
        App.getBus().post(new NotifyMessageAddedEvent(msg));
    }

    @Subscribe
    public void onSendChatMessage(SendChatMessageEvent event) {
        onChatMessageReceived(new ChatMessageReceivedEvent(event.getMessage(), null));
    }

    private void sendAll(String message) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            conn.send(message);
        }
    }

    private void sendAllExcept(String message, WebSocket socket) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            if (!conn.equals(socket)) conn.send(message);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
