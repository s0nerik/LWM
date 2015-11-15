package app.events.chat;

import app.model.chat.ChatMessage
import groovy.transform.Canonical
import groovy.transform.CompileStatic;
import org.java_websocket.WebSocket;

@Canonical
@CompileStatic
class ChatMessageReceivedEvent {
    ChatMessage message
    WebSocket webSocket
}
