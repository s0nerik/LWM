package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.ChatMessagesAvailableEvent;
import com.lwm.app.events.chat.NotifyMessageAddedEvent;
import com.lwm.app.events.chat.ResetUnreadMessagesEvent;
import com.lwm.app.events.chat.SendChatMessageEvent;
import com.lwm.app.events.chat.SetUnreadMessagesEvent;
import com.lwm.app.events.player.binding.LocalPlayerServiceBoundEvent;
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
    private int unreadMessages = 0;

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
    public void onLocalPlayerBound(LocalPlayerServiceBoundEvent event) {
        player = event.getLocalPlayerService();
    }

    @Subscribe
    public void prepareClients(PrepareClientsEvent event) {
        if (webSocketMessageServer.connections().size() != 0) {
            sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.PREPARE).toJson());
        } else {
            App.getBus().post(new AllClientsReadyEvent());
        }
    }

    @Subscribe
    public void allClientsReady(AllClientsReadyEvent event) {
        String pos = String.valueOf(player.getCurrentPosition());
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.START_FROM, pos).toJson());
    }

    @Subscribe
    public void startClients(StartClientsEvent event) {
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.START).toJson());
    }

    @Subscribe
    public void pauseClients(PauseClientsEvent event) {
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.PAUSE).toJson());
    }

    @Subscribe
    public void seekToClients(SeekToClientsEvent event) {
        String pos = String.valueOf(event.getPosition());
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.SEEK_TO, pos).toJson());
    }

    @Produce
    public ChatMessagesAvailableEvent produceMessages() {
        return new ChatMessagesAvailableEvent(chatMessages);
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        unreadMessages += 1;
        ChatMessage msg = event.getMessage();
        chatMessages.add(msg);
        sendAllExcept(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.MESSAGE, new Gson().toJson(msg)).toJson(), event.getWebSocket());
        App.getBus().post(new NotifyMessageAddedEvent(msg));
    }

    @Subscribe
    public void onResetUnreadMessages(ResetUnreadMessagesEvent event) {
        unreadMessages = 0;
    }

    @Subscribe
    public void onSendChatMessage(SendChatMessageEvent event) {
        onChatMessageReceived(new ChatMessageReceivedEvent(event.getMessage(), null));
    }

    @Produce
    public SetUnreadMessagesEvent produceUnreadMessages() {
        return new SetUnreadMessagesEvent(unreadMessages);
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
