package app.websocket
import android.content.SharedPreferences
import android.os.Build
import app.Injector
import app.events.chat.*
import app.events.client.ClientInfoReceivedEvent
import app.events.client.SendReadyEvent
import app.events.client.SocketClosedEvent
import app.events.client.SocketOpenedEvent
import app.model.chat.ChatMessage
import app.player.StreamPlayer
import app.websocket.entities.ClientInfo
import com.google.gson.Gson
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
public class WebSocketMessageClient extends WebSocketClient {

    @Inject
    StreamPlayer player;
    @Inject
    Bus bus;
    @Inject
    SharedPreferences sharedPreferences;

    private List<ChatMessage> chatMessages = new ArrayList<>();
    private int unreadMessages = 0;

    private ClientInfo clientInfo;

    public WebSocketMessageClient(URI serverURI) {
        super(serverURI);
        Injector.inject(this);
        clientInfo = new ClientInfo(name: sharedPreferences.getString("client_name", Build.MODEL));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Debug.d("WebSocketMessageClient: opened with handshake:"
                + "\nStatus: " + handshakedata.getHttpStatus()
                + "\nMessage: " + handshakedata.getHttpStatusMessage());
        bus.register(this);
        bus.post(new SocketOpenedEvent());
    }

    @Override
    public void onMessage(String message) {
        Debug.d("WebSocketMessageClient: \"" + message + "\"");

        SocketMessage socketMessage = SocketMessage.fromJson(message);
        String body = socketMessage.getBody();

        if (socketMessage.getType() == SocketMessage.Type.GET) {
            switch (socketMessage.getMessage()) {
                case CURRENT_POSITION:
                    String pos = String.valueOf(player.getCurrentPosition());
                    send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.CURRENT_POSITION, pos).toJson());
                    break;
                case IS_PLAYING:
                    String isPlaying = String.valueOf(player.isPlaying());
                    send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.IS_PLAYING, isPlaying).toJson());
                    break;
                case CLIENT_INFO:
                    String info = new Gson().toJson(clientInfo, ClientInfo.class);
                    send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.CLIENT_INFO, info).toJson());
                    break;
                default:
                    Debug.e("Can't process message: "+socketMessage.getMessage().name());
            }
        } else if (socketMessage.getType() == SocketMessage.Type.POST) {
            switch (socketMessage.getMessage()) {
                case START:
                    start();
                    break;
                case PAUSE:
                    pause();
                    break;
                case PREPARE:
                    prepare();
                    break;
                case SEEK_TO:
                    seekTo(Integer.valueOf(body));
                    break;
                case START_FROM:
                    startFrom(Integer.valueOf(body));
                    break;
                case MESSAGE:
                    ChatMessage chatMessage = new Gson().fromJson(body, ChatMessage.class);
                    bus.post(new ChatMessageReceivedEvent(chatMessage, getConnection()));
                    break;
                case CLIENT_INFO:
                    ClientInfo clientInfo = new Gson().fromJson(body, ClientInfo.class);
                    bus.post(new ClientInfoReceivedEvent(getConnection(), clientInfo));
                    break;
                default:
                    Debug.e("Can't process message: "+socketMessage.getMessage().name());
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Debug.d("WebSocketMessageClient: closed:\nCode: "+code+" Reason: "+reason);
        bus.post(new SocketClosedEvent());
        bus.unregister(this);
    }

    @Override
    public void onError(Exception ex) {
        Debug.d("WebSocketMessageClient: error:\n" + ex);
    }

    private void startFrom(int pos) {
        player.seekTo(pos);
        player.start();
    }

    private void seekTo(int pos) {
        player.seekTo(pos);
    }

    private void start() {
        player.start();
    }

    private void pause() {
        player.pause();
    }

    private void prepare() {
        player.prepareNewSong();
    }

    @Subscribe
    public void onSendReadyEvent(SendReadyEvent event) {
        send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.READY).toJson());
    }

    @Subscribe
    public void onSendChatMessage(SendChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        send(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.MESSAGE, new Gson().toJson(message)).toJson());
        chatMessages.add(message);
        bus.post(new NotifyMessageAddedEvent(message));
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        unreadMessages += 1;
        ChatMessage msg = event.getMessage();
        chatMessages.add(msg);
        bus.post(new NotifyMessageAddedEvent(msg));
    }

    @Subscribe
    public void onResetUnreadMessages(ResetUnreadMessagesEvent event) {
        unreadMessages = 0;
    }

    @Produce
    public ChatMessagesAvailableEvent produceChatMessages() {
        return new ChatMessagesAvailableEvent(chatMessages);
    }

    @Produce
    public SetUnreadMessagesEvent produceUnreadMessages() {
        return new SetUnreadMessagesEvent(unreadMessages);
    }

}
