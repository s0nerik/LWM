package app.websocket
import app.Injector
import app.Utils
import app.commands.ChangePauseStateCommand
import app.commands.StartPlaybackDelayedCommand
import app.events.chat.ChatMessageReceivedEvent
import app.events.server.ClientConnectedEvent
import app.events.server.ClientDisconnectedEvent
import app.events.server.ClientReadyEvent
import app.helper.PingMeasurer
import app.model.chat.ChatMessage
import app.player.LocalPlayer
import app.websocket.entities.ClientInfo
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import ru.noties.debug.Debug
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.Subject

import javax.inject.Inject
import java.nio.ByteBuffer

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

    private Subject<Pair<WebSocket, SocketMessage>, Pair<WebSocket, SocketMessage>> messages = PublishSubject.create().toSerialized()

    private Observable<Pair<WebSocket, SocketMessage>> getMessages
    private Observable<Pair<WebSocket, SocketMessage>> postMessages

    private Observable<WebSocket> clientReady
    private Observable<WebSocket> clientPong
    private Observable<Pair<WebSocket, ClientInfo>> clientInfo
    private Observable<Pair<WebSocket, ChatMessage>> chatMessage

    private Observable<WebSocket> currentPositionRequest
    private Observable<WebSocket> playbackStatusRequest

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    Bus bus

    WebSocketMessageServer(InetSocketAddress address) {
        super(address)
        Injector.inject this
        initObservables()
        initSubscribers()
    }

    private void initObservables() {
        getMessages = messages.filter { it.value.type == GET }.cast(Pair)
        postMessages = messages.filter { it.value.type == POST }.cast(Pair)

        clientReady = postMessages.filter { it.value.message == READY }.map { it.key }
        clientPong = postMessages.filter { it.value.message == PONG }.map { it.key }

        clientInfo = postMessages.filter { it.value.message == CLIENT_INFO }
                                 .map { new ImmutablePair<WebSocket, ClientInfo>(it.key, ClientInfo.deserialize(it.value.body)) as Pair }

        chatMessage = postMessages.filter { it.value.message == MESSAGE }
                                  .map { new ImmutablePair<WebSocket, ChatMessage>(it.key, ChatMessage.deserialize(it.value.body)) as Pair }

        currentPositionRequest = getMessages.filter { it.value.message == CURRENT_POSITION }
                                            .map { it.key }

        playbackStatusRequest = getMessages.filter { it.value.message == IS_PLAYING }.map { it.key }
    }

    private void initSubscribers() {
        clientReady.subscribe { processReadiness it }
        clientPong.subscribe { pingMeasurers[it].pongReceived() }
        clientInfo.subscribe { processClientInfo it.key, it.value }
        chatMessage.subscribe { bus.post new ChatMessageReceivedEvent(it.value, it.key) }

        currentPositionRequest.subscribe {
            send it, POST, CURRENT_POSITION, Utils.serializeInt(player.currentPosition)
        }

        playbackStatusRequest.subscribe {
            send it, POST, IS_PLAYING, Utils.serializeBool(player.playing)
        }
    }

    @Override
    void onOpen(WebSocket conn, ClientHandshake handshake) {
        Debug.d "connections.size() = ${connections().size()}"

        pingMeasurers[conn] = new PingMeasurer({ send conn, GET, PING, Utils.serializeLong(System.currentTimeMillis()) })
        pingMeasurers[conn].pingWarmupFinished.subscribe {
            send conn, GET, CLIENT_INFO
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
    void onMessage(WebSocket conn, ByteBuffer message) {
        messages.onNext new ImmutablePair<WebSocket, SocketMessage>(conn, SocketMessage.deserialize(message.array()))
        lastMessageTime = System.currentTimeMillis()
    }

    @Override
    void onMessage(WebSocket conn, String message) {
        Debug.e()
    }

    @Override
    void onError(WebSocket conn, Exception ex) {
        Debug.e ex
    }

    private void send(WebSocket conn, SocketMessage.Type type, SocketMessage.Message msg, byte[] body = null) {
        conn.send new SocketMessage(type, msg, body).serialize()
    }

    void sendAll(SocketMessage.Type type, SocketMessage.Message msg, byte[] body = null) {
        for (WebSocket conn : connections()) {
            send conn, type, msg, body
        }
    }

    void sendAllExcept(WebSocket exception, SocketMessage.Type type, SocketMessage.Message msg, byte[] body = null) {
        for (WebSocket conn : connections()) {
            if (!conn.equals(exception)) send conn, type, msg, body
        }
    }

    private void processReadiness(WebSocket conn) {
        Debug.d()

        ready << conn

        bus.post new ClientReadyEvent(conn)

        if(ready.size() == connections().size())
            onEveryoneReady()
    }

    private void onEveryoneReady() {
        def maxAvgPing = pingMeasurers.values().max { PingMeasurer it -> it.average }.average
        Debug.d "maxAvgPing: $maxAvgPing"
        def startTime = System.currentTimeMillis() + maxAvgPing + 2500
        Debug.d "startTime: $startTime"

        bus.post new StartPlaybackDelayedCommand(startTime)

        connections().each {
            send(it, POST, START, Utils.serializeLong(startTime - pingMeasurers[it].average))
        }

        ready.clear()
    }

    private void processClientInfo(WebSocket conn, ClientInfo info) {
        clientInfoMap[conn] = info
        if (player.playing) {
            send conn, POST, PREPARE, Utils.serializeInt(player.currentPosition)
        }
        bus.post new ClientConnectedEvent(info)
    }

    @Subscribe
    void onPauseClients(ChangePauseStateCommand event) {
        Debug.d()
        connections().each {
            send it, POST, PAUSE
        }
    }

}
