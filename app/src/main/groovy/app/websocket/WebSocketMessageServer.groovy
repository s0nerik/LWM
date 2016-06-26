package app.websocket

import app.App
import app.Utils
import app.events.server.ClientConnectedEvent
import app.events.server.ClientDisconnectedEvent
import app.players.LocalPlayer
import com.github.s0nerik.rxbus.RxBus
import app.server.HttpStreamServer
import app.websocket.entities.ClientInfo
import app.websocket.entities.PrepareInfo
import groovy.transform.CompileStatic
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import ru.noties.debug.Debug
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.Subject

import javax.inject.Inject
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import static app.websocket.SocketMessage.Message.*
import static app.websocket.SocketMessage.Type.GET
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
class WebSocketMessageServer extends WebSocketServer {
    private Map<WebSocket, ClientInfo> clientInfoMap = new HashMap<WebSocket, ClientInfo>()
    private Map<WebSocket, Boolean> clientReadinessMap = new HashMap<WebSocket, Boolean>()

//    private Map<WebSocket, PingMeasurer> pingMeasurers = new HashMap<WebSocket, PingMeasurer>()

    private Subject<SocketMessage, SocketMessage> messages = PublishSubject.create().toSerialized()
    private Subject<WebSocket, WebSocket> clientDisconnectedSubject = PublishSubject.create().toSerialized()

    private Observable<SocketMessage> getMessages
    private Observable<SocketMessage> postMessages

    private Map<SocketMessage.Message, Observable<SocketMessage>> get = new HashMap<>()
    private Map<SocketMessage.Message, Observable<SocketMessage>> post = new HashMap<>()

    private static final int startOffset = 5 * 1000

    @Inject
    protected LocalPlayer player

    @Inject
    protected HttpStreamServer httpStreamServer

    boolean started

    WebSocketMessageServer(InetSocketAddress address) {
        super(address)
        App.get().inject this
        initObservables()
        initSubscribers()
    }

    Observable startAsObservable() {
        Observable.just(true)
                  .doOnSubscribe { httpStreamServer.start(); start() }
                  .doOnCompleted { started = true }
    }

    Observable stopAsObservable() {
        Observable.just(true)
                  .doOnSubscribe { httpStreamServer.stop(); stop() }
                  .doOnCompleted { started = false }
    }

    private void initObservables() {
        getMessages = messages.filter { it.type == GET }
        postMessages = messages.filter { it.type == POST }

        SocketMessage.Message.values().each { SocketMessage.Message m ->
            get[m] = getMessages.filter { it.message == m }
            post[m] = postMessages.filter { it.message == m }
        }
    }

    private void initSubscribers() {
        get[CURRENT_POSITION].subscribe {
            send it.socket, POST, CURRENT_POSITION, Utils.serializeInt(player.currentPosition)
        }

        get[IS_PLAYING].subscribe {
            send it.socket, POST, IS_PLAYING, Utils.serializeBool(player.playing)
        }

        get[CURRENT_SONG].subscribe {
            send it.socket, POST, CURRENT_SONG, player.currentSong.serialize()
        }

        post[READY].subscribe {
            clientReadinessMap[it.socket] = true
        }
    }

    Observable pauseClients(Collection<WebSocket> clients = null) {
        Observable.just(null)
                  .doOnSubscribe {
            if (clients != null)
                clients.each { send it, POST, PAUSE }
            else
                sendAll POST, PAUSE
        }
    }

    Observable startClients(Collection<WebSocket> clients = null) {
        def startTime = System.currentTimeMillis() + startOffset
        Observable.timer(startOffset, TimeUnit.MILLISECONDS)
                  .doOnSubscribe {
            if (clients != null)
                clients.each { send it, POST, START, Utils.serializeLong(startTime) }
            else
                sendAll POST, START, Utils.serializeLong(startTime)
        }
    }

    Observable<Collection<WebSocket>> prepareClients(PrepareInfo info) {
        waitForReadyClients().doOnSubscribe { sendAll POST, PREPARE, info.serialize() }
    }

    private Observable prepareClient(WebSocket conn, PrepareInfo info) {
        waitForReadyClient(conn).doOnSubscribe { send conn, POST, PREPARE, info.serialize() }
    }

    private Observable<ClientInfo> requestClientInfo(WebSocket conn) {
        post[CLIENT_INFO].filter { it.socket == conn }
                         .map { ClientInfo.deserialize(it.body) }
                         .doOnSubscribe { send conn, GET, CLIENT_INFO }
    }

    private Observable clientDisconnectAsObservable(WebSocket conn) {
        clientDisconnectedSubject.filter { it == conn }
    }

    private Observable waitForReadyClient(WebSocket conn) {
        post[READY].filter { it.socket == conn }
                   .timeout(10, TimeUnit.SECONDS)
                   .take(1)
    }

    private Observable<Long> measureTimeDiff(WebSocket conn) {
        post[TIMESTAMP].filter { it.socket == conn }
                       .take(1)
                       .map { Utils.deserializeLong(it.body) - System.currentTimeMillis() }
                       .doOnNext { send conn, POST, TIMESTAMP_DIFFERENCE, Utils.serializeLong(it) }
                       .doOnSubscribe { send conn, POST, TIMESTAMP_REQUEST, Utils.serializeLong(System.currentTimeMillis()) }
    }

    private Observable<Long> measureTimeDiffRegularly(WebSocket conn) {
        Observable.interval(1, TimeUnit.SECONDS)
                  .concatMap { measureTimeDiff(conn) }
                  .takeUntil(clientDisconnectAsObservable(conn))
    }

    private Observable<Long> warmupTimeDiff(WebSocket conn) {
        measureTimeDiff(conn).repeat(10)
                             .takeLast(1)
                             .takeUntil(clientDisconnectAsObservable(conn))
    }

    private Observable<Collection<WebSocket>> waitForReadyClients() {
        Observable.<Collection<WebSocket>>defer {
            def connectionsCnt = clientInfoMap.size()
            if (connectionsCnt)
                post[READY].buffer(10, TimeUnit.SECONDS, connectionsCnt).take(1)
            else
                Observable.just(connections())
        }
    }

    private Observable maybePrepareAutostartPlayback(WebSocket conn) {
        if (player.playing)
            prepareClient(conn, PrepareInfo.builder()
                                           .song(player.currentSong)
                                           .position(player.currentPosition + 1000)
                                           .build())
                    .concatMap { startClients([conn]) }
        else
            Observable.empty()
    }

    @Override
    void onOpen(WebSocket conn, ClientHandshake handshake) {
        Debug.d "connections.size() = ${connections().size()}"

        warmupTimeDiff(conn).concatMap { requestClientInfo(conn) }
                            .doOnNext { clientInfoMap[conn] = it; RxBus.post new ClientConnectedEvent(it) }
                            .concatMap { maybePrepareAutostartPlayback(conn) }
                            .concatWith(measureTimeDiffRegularly(conn))
                            .subscribe()
    }

    @Override
    void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Debug.d "connections.size() = ${connections().size()}"
        clientDisconnectedSubject.onNext conn

        RxBus.post new ClientDisconnectedEvent(clientInfoMap[conn])
    }

    @Override
    void onMessage(WebSocket conn, ByteBuffer message) {
        def msg = SocketMessage.deserialize(message.array())
        msg.socket = conn
        messages.onNext msg
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
        for (WebSocket conn : clientInfoMap.keySet()) {
            send conn, type, msg, body
        }
    }

    void sendAllExcept(WebSocket exception, SocketMessage.Type type, SocketMessage.Message msg, byte[] body = null) {
        for (WebSocket conn : clientInfoMap.keySet()) {
            if (!conn.equals(exception)) send conn, type, msg, body
        }
    }

//    @Subscribe
//    void onEvent(ChangePauseStateCommand cmd) {
//        isPlaying = !cmd.pause
////        if (cmd.pause) {
////            pauseClients(connections()).subscribe()
//////            sendAll POST, PAUSE
////        } else {
////            sendAll POST, PREPARE, Utils.serializeInt(player.currentPosition)
////        }
//    }

//    @Subscribe
//    void onSeekTo(SeekToCommand cmd) {
//        sendAll POST, PREPARE, new PrepareInfo(player.currentSong, System.currentTimeMillis(), cmd.position as int, false, true).serialize()
//    }

}
