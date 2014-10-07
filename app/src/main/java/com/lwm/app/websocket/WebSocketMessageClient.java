package com.lwm.app.websocket;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.events.client.SendReadyEvent;
import com.lwm.app.server.async.tasks.SongInfoGetter;
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
    }

    @Override
    public void onMessage(String message) {
        Log.d(App.TAG, "WebSocketMessageClient: \"" + message + "\"");
        switch (message) {
            case SocketMessage.CURRENT_POSITION:
                int pos = player.getCurrentPosition();
                send(String.format(SocketMessage.FORMAT_CURRENT_POSITION, pos));
                break;
            case SocketMessage.START:
                play();
                send(SocketMessageUtils.getOkResponseMessage(SocketMessage.START));
                break;
            case SocketMessage.PAUSE:
                pause();
                send(SocketMessageUtils.getOkResponseMessage(SocketMessage.PAUSE));
                break;
            case SocketMessage.UNPAUSE:
                unpause();
                send(SocketMessageUtils.getOkResponseMessage(SocketMessage.UNPAUSE));
                break;
            case SocketMessage.PREPARE:
                prepare();
                send(SocketMessageUtils.getOkResponseMessage(SocketMessage.PREPARE));
                break;
            case SocketMessage.IS_PLAYING:
                boolean isPlaying = player.isPlaying();
                send(String.format(SocketMessage.FORMAT_IS_PLAYING, isPlaying));
                break;
            default:
                Scanner sc = new Scanner(message);
                if (sc.hasNext()) {
                    String command = sc.next();
                    if (sc.hasNextInt()) {
                        int position = sc.nextInt();
                        if (command.startsWith(SocketMessage.SEEK_TO)) {
                            playFrom(position);
                            send(SocketMessageUtils.getOkResponseMessage(SocketMessage.SEEK_TO));
                        } else if (command.startsWith(SocketMessage.START_FROM)) {
                            seekTo(position);
                            send(SocketMessageUtils.getOkResponseMessage(SocketMessage.START_FROM));
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
        App.getBus().unregister(this);
    }

    @Override
    public void onError(Exception ex) {
        Log.d(App.TAG, "WebSocketMessageClient: error:\n" + ex);
//        App.getBus().unregister(this);
    }

    private void playFrom(int pos) {
        player.seekTo(pos);
//        streamPlayer.start();

        new SongInfoGetter(player.getPlayer()).execute();
    }

    private void seekTo(int pos) {
        player.seekTo(pos);
    }

    private void play() {
        player.start();

        new SongInfoGetter(player.getPlayer()).execute();
    }

    private void pause() {
        player.pause();
    }

    private void unpause() {
        player.start();
    }

    private void prepare() {
        player.prepareNewSong();
    }

    @Subscribe
    public void onSendReadyEvent(SendReadyEvent event) {
        send(SocketMessage.READY);
    }

}
