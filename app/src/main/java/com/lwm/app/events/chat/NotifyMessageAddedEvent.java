package com.lwm.app.events.chat;

import com.lwm.app.model.chat.ChatMessage;

public class NotifyMessageAddedEvent {

    private ChatMessage message;

    public NotifyMessageAddedEvent(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
