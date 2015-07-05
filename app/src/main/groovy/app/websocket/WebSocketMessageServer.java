package app.websocket;

import com.google.gson.Gson;
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

import app.Injector;
import app.events.chat.ChatMessageReceivedEvent;
import app.events.server.AllClientsReadyEvent;
import app.events.server.ClientConnectedEvent;
import app.events.server.ClientDisconnectedEvent;
import app.events.server.ClientReadyEvent;
import app.model.chat.ChatMessage;
import app.player.LocalPlayer;
import app.websocket.entities.ClientInfo;
import ru.noties.debug.Debug;

public class WebSocketMessageServer extends WebSocketServer {

    public static final String URI = "ws://192.168.43.1:8080";

    private static final int TIMEOUT = 10 * 1000; // 10 seconds

    private Set<WebSocket> ready;

    private Map<WebSocket, ClientInfo> clientInfoMap = new HashMap<WebSocket, ClientInfo>();

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
        Debug.d("WebSocketMessageServer: New connection");
        Debug.d("WebSocketMessageServer: connections.size() = "+connections().size());
        conn.send(new SocketMessage(SocketMessage.Type.GET, SocketMessage.Message.CLIENT_INFO).toJson());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Debug.d("WebSocketMessageServer: Close connection");
        Debug.d("WebSocketMessageServer: connections.size() = "+connections().size());
        bus.post(new ClientDisconnectedEvent(clientInfoMap.get(conn)));
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Debug.d("WebSocket message: \"" + message + "\"");

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
                    Debug.e("Can't process message: " + socketMessage.getMessage().name());
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
                    Debug.e("Can't process message: "+socketMessage.getMessage().name());
            }
        }

        lastMessageTime = System.currentTimeMillis();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Debug.e("WebSocketMessageServer onError:\n", ex);
    }

    private void processReadiness(WebSocket conn) {
        if (ready == null) {
            ready = new HashSet<WebSocket>();
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
