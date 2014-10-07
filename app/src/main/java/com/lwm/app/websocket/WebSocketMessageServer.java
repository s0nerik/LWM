package com.lwm.app.websocket;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.service.LocalPlayerService;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class WebSocketMessageServer extends WebSocketServer {

    public static final String URI = "ws://192.168.43.1:8080";

    private LocalPlayerService player;

    public WebSocketMessageServer(InetSocketAddress address) {
        super(address);
        player = App.getLocalPlayerService();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(App.TAG, "WebSocketMessageServer: New connection");
        Log.d(App.TAG, "WebSocketMessageServer: connections.size() = "+connections().size());
        if (player.isPlaying()) {
            conn.send(SocketMessage.PREPARE);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(App.TAG, "WebSocketMessageServer: Close connection");
        Log.d(App.TAG, "WebSocketMessageServer: connections.size() = "+connections().size());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(App.TAG, "WebSocket message: \"" + message + "\"");
        switch (message) {
            case SocketMessage.CURRENT_POSITION:
                int pos = player.getCurrentPosition();
                conn.send(String.format(SocketMessage.FORMAT_CURRENT_POSITION, pos));
                break;
            case SocketMessage.IS_PLAYING:
                boolean isPlaying = player.isPlaying();
                conn.send(String.format(SocketMessage.FORMAT_IS_PLAYING, isPlaying));
                break;
            case SocketMessage.READY:
                conn.send(String.format(SocketMessage.FORMAT_START_FROM, player.getCurrentPosition()));
                break;

// TODO: client playback manipulation
//            case SocketMessage.START:
//                // TODO: start
//                break;
//            case SocketMessage.PAUSE:
//                // TODO: pause
//                break;
//            case SocketMessage.UNPAUSE:
//                // TODO: unpause
//                break;
//            case SocketMessage.PREPARE:
//                // TODO: prepare
//                break;
//            default:
//                Scanner sc = new Scanner(message);
//                if (sc.hasNextInt()) {
//                    int position = sc.nextInt();
//                    if (message.startsWith(SocketMessage.SEEK_TO)) {
//                        // TODO: seek to position
//                    } else if (message.startsWith(SocketMessage.START_FROM)) {
//                        // TODO: start from position
//                    }
//                } else {
//                    Log.e(App.TAG, "Wrong WebSocket message:\n"+message);
//                }
//                sc.close();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

}
