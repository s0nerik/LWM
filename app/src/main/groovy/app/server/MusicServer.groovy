package app.server
import app.Injector
import app.events.chat.*
import app.events.server.*
import app.model.chat.ChatMessage
import app.player.LocalPlayer
import app.websocket.SocketMessage
import app.websocket.WebSocketMessageServer
import com.google.gson.Gson
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import org.java_websocket.WebSocket

import javax.inject.Inject

@CompileStatic
public class MusicServer {

    private WebSocketMessageServer webSocketMessageServer;

    private StreamServer streamServer;

    private LocalPlayer player;

    @Inject
    Bus bus;

    public MusicServer(LocalPlayer player) {
        this.player = player;
        Injector.inject(this);
    }

    private List<ChatMessage> chatMessages = new ArrayList<>();
    private int unreadMessages = 0;

    private boolean started = false;

    public void start() {
        bus.register(this);
        streamServer = new StreamServer(player);
        try {
//            streamServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                webSocketMessageServer = new WebSocketMessageServer(new InetSocketAddress(8080), player);
                webSocketMessageServer.start();
            }
        }).start();
        started = true;
    }

    public void stop() {
        bus.unregister(this);
//        streamServer.stop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocketMessageServer.stop();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    @Subscribe
    public void prepareClients(PrepareClientsEvent event) {
        if (webSocketMessageServer.connections().size() != 0) {
            sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.PREPARE).toJson());
        } else {
            bus.post(new AllClientsReadyEvent());
        }
    }

    @Subscribe
    public void allClientsReady(AllClientsReadyEvent event) {
        String pos = String.valueOf(player.getCurrentPosition());
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.START_FROM, pos).toJson());
    }

    @Subscribe
    public void startClients(StartClientsEvent event) {
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.START).toJson());
    }

    @Subscribe
    public void pauseClients(PauseClientsEvent event) {
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.PAUSE).toJson());
    }

    @Subscribe
    public void seekToClients(SeekToClientsEvent event) {
        String pos = String.valueOf(event.getPosition());
        sendAll(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.SEEK_TO, pos).toJson());
    }

    @Produce
    public ChatMessagesAvailableEvent produceMessages() {
        return new ChatMessagesAvailableEvent(chatMessages);
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        unreadMessages += 1;
        ChatMessage msg = event.getMessage();
        chatMessages.add(msg);
        sendAllExcept(new SocketMessage(SocketMessage.Type.POST, SocketMessage.Message.MESSAGE, new Gson().toJson(msg)).toJson(), event.getWebSocket());
        bus.post(new NotifyMessageAddedEvent(msg));
    }

    @Subscribe
    public void onResetUnreadMessages(ResetUnreadMessagesEvent event) {
        unreadMessages = 0;
    }

    @Subscribe
    public void onSendChatMessage(SendChatMessageEvent event) {
        onChatMessageReceived(new ChatMessageReceivedEvent(event.getMessage(), null));
    }

    @Produce
    public SetUnreadMessagesEvent produceUnreadMessages() {
        return new SetUnreadMessagesEvent(unreadMessages);
    }

    private void sendAll(String message) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            conn.send(message);
        }
    }

    private void sendAllExcept(String message, WebSocket socket) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            if (!conn.equals(socket)) conn.send(message);
        }
    }

}
