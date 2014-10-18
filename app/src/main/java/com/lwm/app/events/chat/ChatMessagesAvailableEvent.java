package com.lwm.app.events.chat;

import com.lwm.app.model.chat.ChatMessage;

import java.util.List;

public class ChatMessagesAvailableEvent {

    private List<ChatMessage> messages;

    public ChatMessagesAvailableEvent(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
