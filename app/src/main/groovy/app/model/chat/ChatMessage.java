package app.model.chat;

import com.google.gson.annotations.Expose;

public class ChatMessage {

    @Expose
    private String message;
    @Expose
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

}
