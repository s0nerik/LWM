package com.lwm.app.events.chat;

import com.lwm.app.model.chat.ChatMessage;

public class ChatMessageReceivedEvent {

    private ChatMessage message;

    public ChatMessageReceivedEvent(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
