package com.lwm.app.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.ClientReadyEvent;
import com.lwm.app.service.LocalPlayerService;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class WebSocketMessageServer extends WebSocketServer {

    public static final String URI = "ws://192.168.43.1:8080";

    private static final int TIMEOUT = 10 * 1000; // 10 seconds

    private List<WebSocket> ready;

    private long lastMessageTime = -1;

    private LocalPlayerService player;

    private Handler handler;

    public WebSocketMessageServer(InetSocketAddress address) {
        super(address);
        player = App.getLocalPlayerService();
        handler = new Handler(Looper.getMainLooper());
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
                processReadiness(conn);
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

        lastMessageTime = System.currentTimeMillis();

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(App.TAG,"WebSocketMessageServer onError:\n", ex);
    }

    private void processReadiness(WebSocket conn) {
        if (ready == null) {
            ready = new ArrayList<>();
        }
        ready.add(conn);

        App.getBus().post(new ClientReadyEvent(conn));

        if(ready.size() == connections().size()) {
            App.getBus().post(new AllClientsReadyEvent());
            ready = null;
        }
    }

}
