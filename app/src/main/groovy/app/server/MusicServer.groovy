package app.server
import app.Daggered
import app.Utils
import app.events.chat.*
import app.events.server.*
import app.model.chat.ChatMessage
import app.player.LocalPlayer
import app.websocket.SocketMessage
import app.websocket.WebSocketMessageServer
import com.github.s0nerik.betterknife.annotations.Profile
import com.google.gson.Gson
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.java_websocket.WebSocket

import javax.inject.Inject

import static app.events.server.MusicServerStateChangedEvent.State.STARTED
import static app.events.server.MusicServerStateChangedEvent.State.STOPPED
import static app.websocket.SocketMessage.Message.MESSAGE
import static app.websocket.SocketMessage.Message.PAUSE
import static app.websocket.SocketMessage.Message.PREPARE
import static app.websocket.SocketMessage.Message.SEEK_TO
import static app.websocket.SocketMessage.Message.START
import static app.websocket.SocketMessage.Message.START_FROM
import static app.websocket.SocketMessage.Type.POST

@CompileStatic
class MusicServer extends Daggered {

    @Inject
    @PackageScope
    StreamServer streamServer

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    LocalPlayer player

    private WebSocketMessageServer webSocketMessageServer

    private List<ChatMessage> chatMessages = new ArrayList<>()
    private int unreadMessages = 0

    boolean started = false

    @Profile
    void start() {
        bus.register this
        try {
            streamServer.start()
        } catch (IOException e) {
            e.printStackTrace()
        }
        webSocketMessageServer = new WebSocketMessageServer(new InetSocketAddress(8080))
        webSocketMessageServer.start()
        setStarted true
    }

    public void stop() {
        bus.unregister this
        streamServer.stop()
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocketMessageServer.stop()
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace()
                }
            }
        }).start()
        setStarted false
    }

    private void setStarted(boolean state) {
        started = state
        bus.post new MusicServerStateChangedEvent(started ? STARTED : STOPPED)
    }

    @Subscribe
    public void prepareClients(PrepareClientsEvent event) {
        if (!webSocketMessageServer.connections().empty) {
            sendAll new SocketMessage(POST, PREPARE, event.position as String).toJson()
        } else {
            bus.post new AllClientsReadyEvent()
        }
    }

    @Subscribe
    public void allClientsReady(AllClientsReadyEvent event) {
        String pos = player.currentPosition as String
        sendAll(new SocketMessage(POST, START_FROM, pos).toJson())
    }

    @Subscribe
    public void pauseClients(PauseClientsEvent event) {
        sendAll(new SocketMessage(POST, PAUSE).toJson())
    }

    @Subscribe
    public void seekToClients(SeekToClientsEvent event) {
        String pos = String.valueOf(event.position)
        sendAll(new SocketMessage(POST, SEEK_TO, pos).toJson())
    }

    @Produce
    public ChatMessagesAvailableEvent produceMessages() {
        return new ChatMessagesAvailableEvent(chatMessages)
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        unreadMessages += 1
        ChatMessage msg = event.getMessage()
        chatMessages.add(msg)
        sendAllExcept(new SocketMessage(POST, MESSAGE, Utils.toJson(msg)).toJson(), event.getWebSocket())
        bus.post(new NotifyMessageAddedEvent(msg))
    }

    @Subscribe
    public void onResetUnreadMessages(ResetUnreadMessagesEvent event) {
        unreadMessages = 0
    }

    @Subscribe
    public void onSendChatMessage(SendChatMessageEvent event) {
        onChatMessageReceived(new ChatMessageReceivedEvent(event.getMessage(), null))
    }

    @Produce
    public SetUnreadMessagesEvent produceUnreadMessages() {
        return new SetUnreadMessagesEvent(unreadMessages)
    }

    @Produce
    public MusicServerStateChangedEvent produceMusicServerState() {
        return new MusicServerStateChangedEvent(started ? STARTED : STOPPED)
    }

    private void sendAll(String message) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            conn.send(message)
        }
    }

    private void sendAllExcept(String message, WebSocket socket) {
        for (WebSocket conn : webSocketMessageServer.connections()) {
            if (!conn.equals(socket)) conn.send(message)
        }
    }

}
