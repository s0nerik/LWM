package app.websocket

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import app.App
import app.Utils
import app.commands.StartPlaybackDelayedCommand
import app.events.chat.*
import app.events.client.ClientInfoReceivedEvent
import app.events.client.SocketClosedEvent
import app.events.client.SocketOpenedEvent
import app.helpers.AveragingCollection
import app.models.chat.ChatMessage
import app.players.StreamPlayer
import com.github.s0nerik.rxbus.RxBus
import app.websocket.entities.ClientInfo
import app.websocket.entities.PrepareInfo
import groovy.transform.CompileStatic
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import ru.noties.debug.Debug
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.Subject
import rx.subscriptions.CompositeSubscription

import javax.inject.Inject
import java.nio.ByteBuffer

import static app.websocket.SocketMessage.Message.*
import static app.websocket.SocketMessage.Type.GET
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
class WebSocketMessageClient extends WebSocketClient {

    @Inject
    protected Context context
    @Inject
    protected StreamPlayer player
    @Inject
    protected SharedPreferences sharedPreferences

    private CompositeSubscription sub = new CompositeSubscription()

    private List<ChatMessage> chatMessages = new ArrayList<>()
    private int unreadMessages = 0

    private ClientInfo clientInfo

    private AveragingCollection<Long> timeDifferences = new AveragingCollection<>(10)

    private Subject<SocketMessage, SocketMessage> messages = PublishSubject.create().toSerialized()

    private Observable<SocketMessage> getMessages
    private Observable<SocketMessage> postMessages

    private Map<SocketMessage.Message, Observable<SocketMessage>> get = new HashMap<>()
    private Map<SocketMessage.Message, Observable<SocketMessage>> post = new HashMap<>()

    private Observable<Long> timeDiffObservable

    WebSocketMessageClient(URI serverURI) {
        super(serverURI)
        App.get().inject this
        clientInfo = new ClientInfo(sharedPreferences.getString("client_name", Build.MODEL))

        initObservables()
        initSubscribers()
    }

    private void initObservables() {
        getMessages = messages.filter { it.type == GET }
        postMessages = messages.filter { it.type == POST }

        SocketMessage.Message.values().each { SocketMessage.Message m ->
            get[m] = getMessages.filter { it.message == m }
            post[m] = postMessages.filter { it.message == m }
        }

        timeDiffObservable = post[TIMESTAMP_REQUEST].map { Utils.deserializeLong(it.body) - System.currentTimeMillis() }
                                                    .doOnNext { sendMessage POST, TIMESTAMP, Utils.serializeLong(System.currentTimeMillis()) }
                                                    .concatMap { t1 ->
            post[TIMESTAMP_DIFFERENCE].take(1).map { ((Utils.deserializeLong(it.body) - (t1 as long)) / 2L) as long }
        }
    }

    private void initSubscribers() {
        get[CURRENT_POSITION].subscribe { sendMessage POST, CURRENT_POSITION, Utils.serializeInt(player.currentPosition) }
        get[IS_PLAYING].subscribe { sendMessage POST, IS_PLAYING, Utils.serializeBool(player.playing) }
        get[CLIENT_INFO].subscribe { sendMessage POST, CLIENT_INFO, clientInfo.serialize() }

        post[START].subscribe { RxBus.post new StartPlaybackDelayedCommand(Utils.deserializeLong(it.body) + timeDifferences.average) }
        post[PAUSE].doOnNext { player.setPaused(true) }.subscribe()
        post[PREPARE].subscribe { prepare PrepareInfo.deserialize(it.body) }
        post[MESSAGE].subscribe { RxBus.post new ChatMessageReceivedEvent(ChatMessage.deserialize(it.body), connection) }
        post[CLIENT_INFO].subscribe { RxBus.post new ClientInfoReceivedEvent(connection, ClientInfo.deserialize(it.body)) }

        timeDiffObservable.subscribe { timeDifferences << it }

        messages.filter { it.message != PING }
                .subscribe { Debug.d "$it.type: $it.message" }
    }

    @Override
    void onOpen(ServerHandshake handshakedata) {
        Debug.d "Status: $handshakedata.httpStatus, Message: $handshakedata.httpStatusMessage"
        initEventHandlers()
        RxBus.post new SocketOpenedEvent()
    }

    private void initEventHandlers() {
        sub.add RxBus.on(SendChatMessageEvent).subscribe(this.&onEvent)
        sub.add RxBus.on(ChatMessageReceivedEvent).subscribe(this.&onEvent)
        sub.add RxBus.on(ResetUnreadMessagesEvent).subscribe(this.&onEvent)

        RxBus.post new ChatMessagesAvailableEvent(chatMessages)
        RxBus.post new SetUnreadMessagesEvent(unreadMessages)
    }

    @Override
    void onMessage(ByteBuffer bytes) {
        messages.onNext SocketMessage.deserialize(bytes.array())
    }

    @Override
    void onMessage(String message) {
        Debug.e()
    }

    void sendMessage(SocketMessage.Type type, SocketMessage.Message msg, byte[] body = null) {
        if (msg != PONG) Debug.d "sendMessage: ${msg}"

        send new SocketMessage(type, msg, body).serialize()
    }

    @Override
    void onClose(int code, String reason, boolean remote) {
        Debug.d "Code: $code, Reason: $reason"
        RxBus.post new SocketClosedEvent()
        sub.clear()
    }

    @Override
    void onError(Exception ex) {
        Debug.e ex as String
    }

    @Override
    void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f)
        Debug.d()
    }

    @Override
    void onWebsocketPong(WebSocket conn, Framedata f) {
        super.onWebsocketPong(conn, f)
        Debug.d()
    }

    private void prepare(PrepareInfo info) {
        def convertObservable = Observable.just(info.song.toRemoteSong(uri.host))
        def prepare = convertObservable.concatMap {
            if (info.seeking && it == player.currentSong)
                return player.pause()
                             .concatMap { player.seekTo(info.position) }
            else
                player.prepareForPosition it, info.position
        }

        prepare.subscribe { sendMessage POST, READY }
    }

    // region Event handlers

    private void onEvent(SendChatMessageEvent event) {
        ChatMessage message = event.message
        sendMessage POST, MESSAGE, message.serialize()
        chatMessages << message
        RxBus.post new NotifyMessageAddedEvent(message)
    }

    private void onEvent(ChatMessageReceivedEvent event) {
        unreadMessages += 1
        ChatMessage msg = event.message;
        chatMessages << msg
        RxBus.post new NotifyMessageAddedEvent(msg)
    }

    private void onEvent(ResetUnreadMessagesEvent event) {
        unreadMessages = 0
    }

    // endregion

}
