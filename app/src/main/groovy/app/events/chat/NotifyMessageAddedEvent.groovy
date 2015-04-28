package app.events.chat;

import app.model.chat.ChatMessage
import groovy.transform.CompileStatic;

@CompileStatic
public class NotifyMessageAddedEvent {

    ChatMessage message;

    public NotifyMessageAddedEvent(ChatMessage message) {
        this.message = message;
    }
}
