package com.lwm.app.websocket;

import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.ChatMessagesAvailableEvent;
import com.lwm.app.events.chat.NotifyMessageAddedEvent;
import com.lwm.app.events.chat.ResetUnreadMessagesEvent;
import com.lwm.app.events.chat.SendChatMessageEvent;
import com.lwm.app.events.chat.SetUnreadMessagesEvent;
import com.lwm.app.events.client.ClientInfoReceivedEvent;
import com.lwm.app.events.client.SendReadyEvent;
import com.lwm.app.events.client.SocketClosedEvent;
import com.lwm.app.events.client.SocketOpenedEvent;
import com.lwm.app.model.chat.ChatMessage;
import com.lwm.app.service.StreamPlayerService;
import com.lwm.app.websocket.entities.ClientInfo;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WebSocketMessageClient extends WebSocketClient {

    private StreamPlayerService player;

    private List<ChatMessage> chatMessages = new ArrayList<>();
    private int unreadMessages = 0;

    public WebSocketMessageClient(URI serverURI) {
        super(serverURI);
        player = App.getStreamPlayerService();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(App.TAG, "WebSocketMessageClient: opened with handshake:"
                + "\nStatus: " + handshakedata.getHttpStatus()
                + "\nMessage: " + handshakedata.getHttpStatusMessage());
        App.getBus().register(this);
        App.getBus().post(new SocketOpenedEvent());
    }

    @Override
    public void onMessage(String message) {
        Log.d(App.TAG, "WebSocketMessageClient: \"" + message + "\"");

        try {
            SocketMessage socketMessage = SocketMessage.valueOf(message);

            switch (socketMessage) {
                case CURRENT_POSITION:
                    int pos = player.getCurrentPosition();
                    send(SocketMessage.formatWithInt(SocketMessage.CURRENT_POSITION, pos));
                    break;
                case START:
                    start();
                    send(SocketMessageUtils.getOkResponseMessage(SocketMessage.START));
                    break;
                case PAUSE:
                    pause();
                    send(SocketMessageUtils.getOkResponseMessage(SocketMessage.PAUSE));
                    break;
                case PREPARE:
                    prepare();
                    send(SocketMessageUtils.getOkResponseMessage(SocketMessage.PREPARE));
                    break;
                case IS_PLAYING:
                    boolean isPlaying = player.isPlaying();
                    send(SocketMessage.formatWithBoolean(SocketMessage.IS_PLAYING, isPlaying));
                    break;
            }

        } catch (IllegalArgumentException e) { // Message with multiple args
            Scanner sc = new Scanner(message);
            if (sc.hasNextLine()) {
                String command = sc.nextLine();

                if (sc.hasNextInt()) {
                    int position = sc.nextInt();

                    try {
                        SocketMessage socketMessage = SocketMessage.valueOf(command);
                        switch (socketMessage) {
                            case SEEK_TO:
                                seekTo(position);
                                send(SocketMessageUtils.getOkResponseMessage(SocketMessage.SEEK_TO));
                                break;
                            case START_FROM:
                                startFrom(position);
                                send(SocketMessageUtils.getOkResponseMessage(SocketMessage.START_FROM));
                                break;
                        }
                    } catch (IllegalArgumentException e1) {
                        Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
                    }
                } else {
                    if (sc.hasNextLine()) {
                        try {
                            SocketMessage socketMessage = SocketMessage.valueOf(command);
                            String json = sc.nextLine();
                            switch(socketMessage) {
                                case MESSAGE:
                                    ChatMessage chatMessage = new Gson().fromJson(json, ChatMessage.class);
                                    App.getBus().post(new ChatMessageReceivedEvent(chatMessage, getConnection()));
                                    break;
                                case CLIENT_INFO:
                                    ClientInfo clientInfo = new Gson().fromJson(json, ClientInfo.class);
                                    App.getBus().post(new ClientInfoReceivedEvent(getConnection(), clientInfo));
                                    break;
                                default:
                                    Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
                            }
                        } catch (IllegalArgumentException e1) {
                            Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
                        }
                    }
                }
            } else {
                Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
            }
            sc.close();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(App.TAG, "WebSocketMessageClient: closed:\nCode: "+code+" Reason: "+reason);
        App.getBus().post(new SocketClosedEvent());
        App.getBus().unregister(this);
    }

    @Override
    public void onError(Exception ex) {
        Log.d(App.TAG, "WebSocketMessageClient: error:\n" + ex);
    }

    private void startFrom(int pos) {
        player.seekTo(pos);
        player.start();
    }

    private void seekTo(int pos) {
        player.seekTo(pos);
    }

    private void start() {
        player.start();
    }

    private void pause() {
        player.pause();
    }

    private void prepare() {
        player.prepareNewSong();
    }

    @Subscribe
    public void onSendReadyEvent(SendReadyEvent event) {
        send(SocketMessage.getStringToSend(SocketMessage.READY));
    }

    @Subscribe
    public void onSendChatMessage(SendChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        send(SocketMessage.formatWithString(SocketMessage.MESSAGE, new Gson().toJson(message)));
        chatMessages.add(message);
        App.getBus().post(new NotifyMessageAddedEvent(message));
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        unreadMessages += 1;
        ChatMessage msg = event.getMessage();
        chatMessages.add(msg);
        App.getBus().post(new NotifyMessageAddedEvent(msg));
    }

    @Subscribe
    public void onResetUnreadMessages(ResetUnreadMessagesEvent event) {
        unreadMessages = 0;
    }

    @Produce
    public ChatMessagesAvailableEvent produceChatMessages() {
        return new ChatMessagesAvailableEvent(chatMessages);
    }

    @Produce
    public SetUnreadMessagesEvent produceUnreadMessages() {
        return new SetUnreadMessagesEvent(unreadMessages);
    }

}
