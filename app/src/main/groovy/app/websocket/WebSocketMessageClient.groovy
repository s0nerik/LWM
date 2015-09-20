package app.websocket
import android.content.SharedPreferences
import android.net.Uri
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
import app.server.StreamServer
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

    private final Uri STREAM_URI =
            Uri.parse "http://${uri.host}:${StreamServer.PORT}${StreamServer.Method.STREAM}"

    private List<ChatMessage> chatMessages = new ArrayList<>()
    private int unreadMessages = 0

    private ClientInfo clientInfo

    public WebSocketMessageClient(URI serverURI) {
        super(serverURI)
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
                    sendMessage POST, CURRENT_POSITION, pos
                    break
                case IS_PLAYING:
                    String isPlaying = player.playing as String
                    sendMessage POST, IS_PLAYING, isPlaying
                    break
                case CLIENT_INFO:
                    String info = Utils.toJson clientInfo
                    sendMessage POST, CLIENT_INFO, info
                    break
                default:
                    Debug.e "Can't process message: ${socketMessage.message.name()}"
            }
        } else if (socketMessage.type == POST) {
            switch (socketMessage.message) {
                case START:
                    player.paused = false
                    break
                case PAUSE:
                    player.paused = true
                    break
                case PREPARE:
                    prepare body as int
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

    private sendMessage(SocketMessage.Type type, SocketMessage.Message msg, String body = null) {
        send new SocketMessage(type, msg, body).toJson()
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
//        player.prepare STREAM_URI
        player.paused = false
    }

    private void seekTo(int pos) {
        player.paused = true
        player.seekTo(pos)
    }

    private void prepare(int position) {
        player.paused = true
        player.prepare STREAM_URI, true
        player.seekTo position
    }

    @Subscribe
    void onSendReadyEvent(SendReadyEvent event) {
        sendMessage POST, READY
    }

    @Subscribe
    void onSendChatMessage(SendChatMessageEvent event) {
        ChatMessage message = event.message
        sendMessage POST, MESSAGE, Utils.toJson(message)
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
