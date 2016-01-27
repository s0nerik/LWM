package app.websocket

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import app.Injector
import app.Utils
import app.commands.StartPlaybackDelayedCommand
import app.events.chat.*
import app.events.client.ClientInfoReceivedEvent
import app.events.client.SocketClosedEvent
import app.events.client.SocketOpenedEvent
import app.helper.AveragingCollection
import app.model.chat.ChatMessage
import app.player.StreamPlayer
import app.websocket.entities.ClientInfo
import app.websocket.entities.PrepareInfo
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
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
@PackageScope(PackageScopeTarget.FIELDS)
public class WebSocketMessageClient extends WebSocketClient {

    @Inject
    Context context
    @Inject
    StreamPlayer player
    @Inject
    Bus bus
    @Inject
    SharedPreferences sharedPreferences

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
        Injector.inject this
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

        post[START].subscribe { bus.post new StartPlaybackDelayedCommand(Utils.deserializeLong(it.body) - timeDifferences.average) }
        post[PAUSE].doOnNext { player.setPaused(true) }.subscribe()
        post[PREPARE].subscribe { prepare PrepareInfo.deserialize(it.body) }
        post[MESSAGE].subscribe { bus.post new ChatMessageReceivedEvent(ChatMessage.deserialize(it.body), connection) }
        post[CLIENT_INFO].subscribe { bus.post new ClientInfoReceivedEvent(connection, ClientInfo.deserialize(it.body)) }

        timeDiffObservable.subscribe { timeDifferences << it }

        messages.filter { it.message != PING }
                .subscribe { Debug.d "$it.type: $it.message" }
    }

    @Override
    void onOpen(ServerHandshake handshakedata) {
        Debug.d "Status: $handshakedata.httpStatus, Message: $handshakedata.httpStatusMessage"
        bus.register this
        bus.post new SocketOpenedEvent()
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
        bus.post new SocketClosedEvent()
        bus.unregister this
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
        int offset = 10000

        Observable<Object> prepare
        if (!info.seeking || player.currentSong != info.song) {
            def convertObservable = Observable.just(info.song.toRemoteSong(uri.host))

            if (info.autostart) {
                prepare = convertObservable.concatMap { player.prepareForPosition it, info.position + offset }
                                           .ignoreElements()
            } else {
                prepare = convertObservable.concatMap { player.prepareForPosition it, info.position }
            }
        } else {
            prepare = player.prepareForPosition info.song, info.position
        }

        prepare = prepare.doOnCompleted { sendMessage POST, READY }

        if (info.autostart && !info.seeking) {
            prepare = prepare.mergeWith(Observable.timer(offset, TimeUnit.MILLISECONDS).map { (Object) null })
                             .concatWith(player.start())
        }

        prepare.subscribe()
    }

    //region Chat

    @Subscribe
    void onSendChatMessage(SendChatMessageEvent event) {
        ChatMessage message = event.message
        sendMessage POST, MESSAGE, message.serialize()
        chatMessages << message
        bus.post new NotifyMessageAddedEvent(message)
    }

    @Subscribe
    void onChatMessageReceived(ChatMessageReceivedEvent event) {
        unreadMessages += 1
        ChatMessage msg = event.message;
        chatMessages << msg
        bus.post new NotifyMessageAddedEvent(msg)
    }

    @Subscribe
    void onResetUnreadMessages(ResetUnreadMessagesEvent event) {
        unreadMessages = 0
    }

    @Produce
    ChatMessagesAvailableEvent produceChatMessages() {
        new ChatMessagesAvailableEvent(chatMessages)
    }

    @Produce
    SetUnreadMessagesEvent produceUnreadMessages() {
        new SetUnreadMessagesEvent(unreadMessages)
    }

    //endregion

}
