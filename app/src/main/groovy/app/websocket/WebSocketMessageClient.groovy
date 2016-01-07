package app.websocket
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import app.Injector
import app.Utils
import app.commands.StartPlaybackDelayedCommand
import app.events.chat.*
import app.events.client.ClientInfoReceivedEvent
import app.events.client.SocketClosedEvent
import app.events.client.SocketOpenedEvent
import app.helper.TimeDifferenceMeasurer
import app.model.Song
import app.model.chat.ChatMessage
import app.player.StreamPlayer
import app.server.StreamServer
import app.websocket.entities.ClientInfo
import com.koushikdutta.ion.Ion
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import ru.noties.debug.Debug
import rx.Observable
import rx.Observer
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

    private final Uri SONG_INFO_URI =
            Uri.parse "http://${uri.host}:${StreamServer.PORT}${StreamServer.Method.CURRENT_INFO}"

    private List<ChatMessage> chatMessages = new ArrayList<>()
    private int unreadMessages = 0

    private ClientInfo clientInfo

    private TimeDifferenceMeasurer timeDifferenceMeasurer = new TimeDifferenceMeasurer()

    private Subject<SocketMessage, SocketMessage> messages = PublishSubject.create().toSerialized()

    private Observable<SocketMessage> getMessages
    private Observable<SocketMessage> postMessage

    private Observable<SocketMessage> getCurrentPosition
    private Observable<SocketMessage> getIsPlaying
    private Observable<SocketMessage> getClientInfo
    private Observable<SocketMessage> getPing

    private Observable<SocketMessage> postStart
    private Observable<SocketMessage> postPause
    private Observable<SocketMessage> postPrepare
    private Observable<SocketMessage> postSeekTo
    private Observable<SocketMessage> postClientInfo

    private Observable<SocketMessage> postChatMessage

    WebSocketMessageClient(URI serverURI) {
        super(serverURI)
        Injector.inject this
        clientInfo = new ClientInfo(sharedPreferences.getString("client_name", Build.MODEL))

        initObservables()
        initSubscribers()
    }

    private void initObservables() {
        getMessages = messages.filter { it.type == GET }
        postMessage = messages.filter { it.type == POST }

        getPing = getMessages.filter { it.message == PING }
        getClientInfo = getMessages.filter { it.message == CLIENT_INFO }
        getIsPlaying = getMessages.filter { it.message == IS_PLAYING }
        getCurrentPosition = getMessages.filter { it.message == CURRENT_POSITION }

        postStart = postMessage.filter { it.message == START }
        postPause = postMessage.filter { it.message == PAUSE }
        postPrepare = postMessage.filter { it.message == PREPARE }
        postSeekTo = postMessage.filter { it.message == SEEK_TO }
        postClientInfo = postMessage.filter { it.message == CLIENT_INFO }

        postChatMessage = postMessage.filter { it.message == MESSAGE }
    }

    private void initSubscribers() {
        getCurrentPosition.subscribe { sendMessage POST, CURRENT_POSITION, Utils.serializeInt(player.currentPosition) }
        getIsPlaying.subscribe { sendMessage POST, IS_PLAYING, Utils.serializeBool(player.playing) }
        getClientInfo.subscribe { sendMessage POST, CLIENT_INFO, clientInfo.serialize() }

        getPing.doOnNext { timeDifferenceMeasurer.add it.body as long }
               .doOnNext { Debug.d "time difference: ${timeDifferenceMeasurer.difference}" }
               .subscribe { sendMessage POST, PONG, Utils.serializeLong(System.currentTimeMillis()) }

        postStart.subscribe { bus.post new StartPlaybackDelayedCommand(timeDifferenceMeasurer.toLocalTime(it.body as long)) }
        postPause.doOnNext { player.setPaused(true) }.subscribe()
        postPrepare.subscribe { prepare it.body as int }
        postSeekTo.subscribe { seekTo it.body as int }
        postChatMessage.subscribe { bus.post new ChatMessageReceivedEvent(ChatMessage.deserialize(it.body), connection) }
        postClientInfo.subscribe { bus.post new ClientInfoReceivedEvent(connection, ClientInfo.deserialize(it.body)) }


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

    private void seekTo(int pos) {
        prepare pos, true
    }

    private void prepare(int position, boolean seeking = false) {
        Observable prepare
        if (!seeking) {
            def loadSong = Observable.create({ Observer<String> observer ->
                try {
                    def songStr = Ion.with(context).load(SONG_INFO_URI as String).asString().get(5, TimeUnit.SECONDS)
                    observer.onNext(songStr)
                    observer.onCompleted()
                } catch (e) {
                    observer.onError(e)
                }
            } as Observable.OnSubscribe<String>)

            prepare = loadSong
                    .map { Song.fromJson(it) }
                    .map { it.toRemoteSong(uri.host) }
                    .doOnNext { player.song = it }
                    .concatMap { player.prepareForPosition position }
        } else {
            prepare = player.prepareForPosition(position)
        }

        prepare.doOnCompleted { sendMessage POST, READY }
               .subscribe()
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
