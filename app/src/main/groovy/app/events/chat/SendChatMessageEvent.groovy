package app.events.chat;

import app.model.chat.ChatMessage
import groovy.transform.CompileStatic;

@CompileStatic
public class SendChatMessageEvent {

    ChatMessage message;

    public SendChatMessageEvent(ChatMessage message) {
        this.message = message;
    }
}
