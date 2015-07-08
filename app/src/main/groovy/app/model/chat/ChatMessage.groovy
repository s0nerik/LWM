package app.model.chat;

import com.google.gson.annotations.Expose
import groovy.transform.CompileStatic;

@CompileStatic
public class ChatMessage {

    @Expose
    String message

    @Expose
    String author

    public ChatMessage(String author, String message) {
        this.author = author;
        this.message = message;
    }

}
