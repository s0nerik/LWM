package app.websocket
import android.content.SharedPreferences
import android.os.Build
import app.Injector
import app.Utils
import app.events.chat.*
import app.events.client.ClientInfoReceivedEvent
import app.events.client.SendReadyEvent
import app.events.client.SocketClosedEvent
import app.events.client.SocketOpenedEvent
import app.model.chat.ChatMessage
import app.player.StreamPlayer
import app.websocket.entities.ClientInfo
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import ru.noties.debug.Debug

import javax.inject.Inject

import static app.websocket.SocketMessage.Message.*
import static app.websocket.SocketMessage.Type.GET
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class WebSocketMessageClient extends WebSocketClient {

    @Inject
    StreamPlayer player
    @Inject
    Bus bus
    @Inject
    SharedPreferences sharedPreferences

    private List<ChatMessage> chatMessages = new ArrayList<>()
    private int unreadMessages = 0

    private ClientInfo clientInfo

    public WebSocketMessageClient(URI serverURI) {
        super(serverURI);
        Injector.inject this
        clientInfo = new ClientInfo(name: sharedPreferences.getString("client_name", Build.MODEL))
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Debug.d "Status: $handshakedata.httpStatus, Message: $handshakedata.httpStatusMessage"
        bus.register this
        bus.post new SocketOpenedEvent()
    }

    @Override
    public void onMessage(String message) {
        Debug.d "$message"

        SocketMessage socketMessage = Utils.fromJson message
        String body = socketMessage.body

        if (socketMessage.type == GET) {
            switch (socketMessage.message) {
                case CURRENT_POSITION:
                    String pos = player.currentPosition as String
                    send new SocketMessage(POST, CURRENT_POSITION, pos).toJson()
                    break
                case IS_PLAYING:
                    String isPlaying = player.playing as String
                    send new SocketMessage(POST, IS_PLAYING, isPlaying).toJson()
                    break
                case CLIENT_INFO:
                    String info = Utils.toJson clientInfo
                    send new SocketMessage(POST, CLIENT_INFO, info).toJson()
                    break
                default:
                    Debug.e "Can't process message: ${socketMessage.message.name()}"
            }
        } else if (socketMessage.type == POST) {
            switch (socketMessage.message) {
                case START:
                    start()
                    break
                case PAUSE:
                    pause()
                    break
                case PREPARE:
                    prepare()
                    break
                case SEEK_TO:
                    seekTo body as int
                    break
                case START_FROM:
                    startFrom body as int
                    break
                case MESSAGE:
                    ChatMessage chatMessage = Utils.fromJson body
                    bus.post new ChatMessageReceivedEvent(chatMessage, connection)
                    break
                case CLIENT_INFO:
                    ClientInfo clientInfo = Utils.fromJson body
                    bus.post new ClientInfoReceivedEvent(connection, clientInfo)
                    break
                default:
                    Debug.e "Can't process message: ${socketMessage.message.name()}"
            }
        }
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

    private void startFrom(int pos) {
        player.seekTo(pos)
        player.unpause()
    }

    private void seekTo(int pos) {
        player.seekTo(pos)
    }

    private void start() {
        player.unpause()
    }

    private void pause() {
        player.pause()
    }

    private void prepare() {
        player.pause()
        player.prepare()
    }

    @Subscribe
    void onSendReadyEvent(SendReadyEvent event) {
        send new SocketMessage(POST, READY).toJson()
    }

    @Subscribe
    void onSendChatMessage(SendChatMessageEvent event) {
        ChatMessage message = event.message
        send new SocketMessage(POST, MESSAGE, Utils.toJson(message)).toJson()
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

}
