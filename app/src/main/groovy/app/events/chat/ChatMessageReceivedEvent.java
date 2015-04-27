package app.events.chat;

import app.model.chat.ChatMessage;

import org.java_websocket.WebSocket;

public class ChatMessageReceivedEvent {

    private ChatMessage message;
    private WebSocket webSocket;

    public ChatMessageReceivedEvent(ChatMessage message, WebSocket webSocket) {
        this.message = message;
        this.webSocket = webSocket;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }
}
