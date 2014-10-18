package com.lwm.app.model.chat;

public class ChatMessage {

    private String message;
    private String author;

    public ChatMessage(String author, String message) {
        this.author = author;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return author + "\n" + message;
    }
}
