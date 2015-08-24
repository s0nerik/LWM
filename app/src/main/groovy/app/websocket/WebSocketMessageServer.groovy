package app.websocket
import app.Injector
import app.Utils
import app.events.chat.ChatMessageReceivedEvent
import app.events.server.AllClientsReadyEvent
import app.events.server.ClientConnectedEvent
import app.events.server.ClientDisconnectedEvent
import app.events.server.ClientReadyEvent
import app.model.chat.ChatMessage
import app.player.LocalPlayer
import app.websocket.entities.ClientInfo
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import ru.noties.debug.Debug

import javax.inject.Inject

import static app.websocket.SocketMessage.Message.*
import static app.websocket.SocketMessage.Type.GET
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
class WebSocketMessageServer extends WebSocketServer {

    private static final int TIMEOUT = 10 * 1000 // 10 seconds

    private Set<WebSocket> ready

    private Map<WebSocket, ClientInfo> clientInfoMap = new HashMap<WebSocket, ClientInfo>()

    private long lastMessageTime = -1

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    Bus bus

    WebSocketMessageServer(InetSocketAddress address) {
        super(address)
        Injector.inject this
    }

    @Override
    void onOpen(WebSocket conn, ClientHandshake handshake) {
        Debug.d "connections.size() = ${connections().size()}"
        conn.send new SocketMessage(GET, CLIENT_INFO).toJson()
    }

    @Override
    void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Debug.d "connections.size() = ${connections().size()}"
        bus.post new ClientDisconnectedEvent(clientInfoMap[conn])
    }

    @Override
    void onMessage(WebSocket conn, String message) {
        Debug.d "$message"

        SocketMessage socketMessage = Utils.fromJson message
        String body = socketMessage.body

        if (socketMessage.type == GET) {
            switch (socketMessage.message) {
                case CURRENT_POSITION:
                    String pos = player.currentPosition as String
                    conn.send(new SocketMessage(POST, CURRENT_POSITION, pos).toJson())
                    break
                case IS_PLAYING:
                    String isPlaying = player.playing as String
                    conn.send new SocketMessage(POST, IS_PLAYING, isPlaying).toJson()
                    break
                default:
                    Debug.e "Can't process message: ${socketMessage.message.name()}"
            }
        } else if (socketMessage.type == POST) {
            switch (socketMessage.message) {
                case READY:
                    processReadiness(conn)
                    break
                case CLIENT_INFO:
                    processClientInfo conn, Utils.<ClientInfo>fromJson(body)
                    break
                case MESSAGE:
                    ChatMessage chatMessage = Utils.fromJson body
                    bus.post new ChatMessageReceivedEvent(chatMessage, conn)
                    break
                default:
                    Debug.e "Can't process message: ${socketMessage.message.name()}"
            }
        }

        lastMessageTime = System.currentTimeMillis()
    }

    @Override
    void onError(WebSocket conn, Exception ex) {
        Debug.e ex
    }

    private void processReadiness(WebSocket conn) {
        if (ready == null) {
            ready = new HashSet<WebSocket>();
        }
        ready << conn

        bus.post new ClientReadyEvent(conn)

        if(ready.size() == connections().size()) {
            bus.post new AllClientsReadyEvent()
            ready = null
        }
    }

    private void processClientInfo(WebSocket conn, ClientInfo info) {
        clientInfoMap[conn] = info
        if (player.playing) {
            conn.send new SocketMessage(POST, PREPARE).toJson()
        }
        bus.post new ClientConnectedEvent(info)
    }

}
