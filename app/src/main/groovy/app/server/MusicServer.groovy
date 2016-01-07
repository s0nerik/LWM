package app.server
import app.Daggered
import app.Utils
import app.commands.ChangePauseStateCommand
import app.commands.PrepareClientsCommand
import app.commands.SeekToCommand
import app.commands.StartPlaybackDelayedCommand
import app.events.chat.*
import app.events.server.MusicServerStateChangedEvent
import app.model.chat.ChatMessage
import app.player.LocalPlayer
import app.websocket.WebSocketMessageServer
import com.github.s0nerik.betterknife.annotations.Profile
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

import static app.events.server.MusicServerStateChangedEvent.State.STARTED
import static app.events.server.MusicServerStateChangedEvent.State.STOPPED
import static app.websocket.SocketMessage.Message.MESSAGE
import static app.websocket.SocketMessage.Message.PAUSE
import static app.websocket.SocketMessage.Message.PREPARE
import static app.websocket.SocketMessage.Message.SEEK_TO
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
        Thread.start {
            try {
                webSocketMessageServer.stop()
            } catch (IOException | InterruptedException e) {
                e.printStackTrace()
            }
        }
        setStarted false
    }

    private void setStarted(boolean state) {
        started = state
        bus.post new MusicServerStateChangedEvent(started ? STARTED : STOPPED)
    }

    @Subscribe
    public void prepareClients(PrepareClientsCommand event) {
        if (!webSocketMessageServer.connections().empty) {
            webSocketMessageServer.sendAll POST, PREPARE, event.position as String
        } else {
            bus.post new StartPlaybackDelayedCommand()
        }
    }

    @Subscribe
    void onChangePauseState(ChangePauseStateCommand cmd) {
        if (cmd.pause) {
            webSocketMessageServer.sendAll POST, PAUSE
        } else {
            webSocketMessageServer.sendAll POST, PREPARE, player.currentPosition as String
        }
    }

    @Subscribe
    void onSeekTo(SeekToCommand cmd) {
        webSocketMessageServer.sendAll POST, SEEK_TO, cmd.position as String
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
        webSocketMessageServer.sendAllExcept event.webSocket, POST, MESSAGE, Utils.toJson(msg)
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

}
