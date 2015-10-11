package app.websocket
import app.Injector
import app.Utils
import app.commands.StartPlaybackCommand
import app.events.chat.ChatMessageReceivedEvent
import app.events.server.ClientConnectedEvent
import app.events.server.ClientDisconnectedEvent
import app.events.server.ClientReadyEvent
import app.commands.ChangePauseStateCommand
import app.helper.PingMeasurer
import app.model.chat.ChatMessage
import app.player.LocalPlayer
import app.websocket.entities.ClientInfo
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
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

    private Set<WebSocket> ready = new HashSet<>()

    private Map<WebSocket, ClientInfo> clientInfoMap = new HashMap<WebSocket, ClientInfo>()

    private Map<WebSocket, PingMeasurer> pingMeasurers = new HashMap<WebSocket, PingMeasurer>()

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

        pingMeasurers[conn] = new PingMeasurer({ sendMessage conn, GET, PING, System.currentTimeMillis() as String })
        pingMeasurers[conn].pingWarmupFinished.subscribe {
            conn.send new SocketMessage(GET, CLIENT_INFO).toJson()
        }
        pingMeasurers[conn].start()
    }

    @Override
    void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Debug.d "connections.size() = ${connections().size()}"
        bus.post new ClientDisconnectedEvent(clientInfoMap[conn])

        pingMeasurers[conn].stop()
        pingMeasurers.remove(conn)
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
                    sendMessage conn, POST, CURRENT_POSITION, pos
                    break
                case IS_PLAYING:
                    String isPlaying = player.playing as String
                    sendMessage conn, POST, IS_PLAYING, isPlaying
                    break
                default:
                    Debug.e "Can't process message: ${socketMessage.message.name()}"
            }
        } else if (socketMessage.type == POST) {
            switch (socketMessage.message) {
                case READY:
                    processReadiness conn
                    break
                case PONG:
                    pingMeasurers[conn].pongReceived()
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

    private static void sendMessage(WebSocket conn, SocketMessage.Type type, SocketMessage.Message msg, String body = null) {
        conn.send(new SocketMessage(type, msg, body).toJson())
    }

    private void processReadiness(WebSocket conn) {
        ready << conn

        bus.post new ClientReadyEvent(conn)

        if(ready.size() == connections().size())
            onEveryoneReady()
    }

    private void onEveryoneReady() {
        def startTime = System.currentTimeMillis() + pingMeasurers.values().max { PingMeasurer it -> it.average }.average + 250

        bus.post new StartPlaybackCommand(startTime)

        connections().each {
            sendMessage(it, POST, START, startTime as String)
        }

        ready.clear()
    }

    private void processClientInfo(WebSocket conn, ClientInfo info) {
        clientInfoMap[conn] = info
        if (player.playing) {
            conn.send new SocketMessage(POST, PREPARE, player.currentPosition as String).toJson()
        }
        bus.post new ClientConnectedEvent(info)
    }

    @Subscribe
    void onPauseClients(ChangePauseStateCommand event) {
        Debug.d()
        connections().each {
            sendMessage it, POST, PAUSE
        }
    }

    void sendAll(String message) {
        for (WebSocket conn : connections()) {
            conn.send(message)
        }
    }

    void sendAllExcept(String message, WebSocket socket) {
        for (WebSocket conn : connections()) {
            if (!conn.equals(socket)) conn.send(message)
        }
    }

}
