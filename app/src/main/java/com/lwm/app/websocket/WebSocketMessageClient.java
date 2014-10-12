package com.lwm.app.websocket;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.events.client.SendReadyEvent;
import com.lwm.app.events.client.SocketClosedEvent;
import com.lwm.app.events.client.SocketOpenedEvent;
import com.lwm.app.service.StreamPlayerService;
import com.squareup.otto.Subscribe;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Scanner;

public class WebSocketMessageClient extends WebSocketClient {

    private StreamPlayerService player;

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

        } catch (IllegalArgumentException e) { // Message with colon
            Scanner sc = new Scanner(message);
            if (sc.hasNext()) {
                String command = sc.next();

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
                    Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
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

}
