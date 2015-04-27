package app.events.chat;

import app.model.chat.ChatMessage;

public class NotifyMessageAddedEvent {

    private ChatMessage message;

    public NotifyMessageAddedEvent(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
