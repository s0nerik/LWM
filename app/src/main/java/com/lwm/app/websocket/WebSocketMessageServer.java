package com.lwm.app.websocket;

import android.util.Log;

import com.google.gson.Gson;
import com.lwm.app.App;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.events.server.ClientReadyEvent;
import com.lwm.app.model.chat.ChatMessage;
import com.lwm.app.service.LocalPlayerService;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WebSocketMessageServer extends WebSocketServer {

    public static final String URI = "ws://192.168.43.1:8080";

    private static final int TIMEOUT = 10 * 1000; // 10 seconds

    private List<WebSocket> ready;

    private long lastMessageTime = -1;

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
            conn.send(SocketMessage.getStringToSend(SocketMessage.PREPARE));
        }
        App.getBus().post(new ClientConnectedEvent(""));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(App.TAG, "WebSocketMessageServer: Close connection");
        Log.d(App.TAG, "WebSocketMessageServer: connections.size() = "+connections().size());
        App.getBus().post(new ClientDisconnectedEvent(""));
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(App.TAG, "WebSocket message: \"" + message + "\"");

        try {
            SocketMessage socketMessage = SocketMessage.valueOf(message);

            switch (socketMessage) {
                case CURRENT_POSITION:
                    int pos = player.getCurrentPosition();
                    conn.send(SocketMessage.formatWithInt(SocketMessage.CURRENT_POSITION, pos));
                    break;
                case IS_PLAYING:
                    boolean isPlaying = player.isPlaying();
                    conn.send(SocketMessage.formatWithBoolean(SocketMessage.IS_PLAYING, isPlaying));
                    break;
                case READY:
                    processReadiness(conn);
                    break;

//  TODO: client playback manipulation
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
//                if (sc.hasNext()) {
//                    String command = sc.next();
//                    if (sc.hasNextInt()) {
//                        int position = sc.nextInt();
//                        if (command.startsWith(SocketMessage.SEEK_TO)) {
//                            playFrom(position);
//                            send(SocketMessageUtils.getOkResponseMessage(SocketMessage.SEEK_TO));
//                        } else if (command.startsWith(SocketMessage.START_FROM)) {
//                            seekTo(position);
//                            send(SocketMessageUtils.getOkResponseMessage(SocketMessage.START_FROM));
//                        }
//                    } else {
//                        Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
//                    }
//                } else {
//                    Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
//                }
//                sc.close();
            }
        } catch (IllegalArgumentException e) { // Message with space or newline
            Scanner sc = new Scanner(message);
            if (sc.hasNextLine()) {
                String command = sc.nextLine();

                if (sc.hasNextLine()) {
                    try {
                        SocketMessage socketMessage = SocketMessage.valueOf(command);
                        if (socketMessage == SocketMessage.MESSAGE) {
                            ChatMessage chatMessage = new Gson().fromJson(sc.nextLine(), ChatMessage.class);
                            App.getBus().post(new ChatMessageReceivedEvent(chatMessage, conn));
                        } else {
                            Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
                        }
                    } catch (IllegalArgumentException e1) {
                        Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
                    }
                }
            } else {
                Log.e(App.TAG, "Wrong WebSocket message:\n" + message);
            }
            sc.close();
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
