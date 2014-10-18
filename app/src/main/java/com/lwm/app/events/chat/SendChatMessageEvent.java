package com.lwm.app.events.chat;

import com.lwm.app.model.chat.ChatMessage;

public class SendChatMessageEvent {

    private ChatMessage message;

    public SendChatMessageEvent(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
