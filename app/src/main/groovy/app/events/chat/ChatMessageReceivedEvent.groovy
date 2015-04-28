package app.events.chat;

import app.model.chat.ChatMessage
import groovy.transform.CompileStatic;
import org.java_websocket.WebSocket;

@CompileStatic
public class ChatMessageReceivedEvent {

    ChatMessage message;
    WebSocket webSocket;

    public ChatMessageReceivedEvent(ChatMessage message, WebSocket webSocket) {
        this.message = message;
        this.webSocket = webSocket;
    }
}
