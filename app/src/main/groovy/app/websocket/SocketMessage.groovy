package app.websocket;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

public class SocketMessage {
    public static enum Message {
        START_FROM, SEEK_TO,
        CURRENT_POSITION, IS_PLAYING,
        OK, ERROR,
        PAUSE, UNPAUSE, START,
        PREPARE, READY,
        MESSAGE, CLIENT_INFO
    }

    public static enum Type {
        GET, POST
    }

    @Expose
    Type type;

    @Expose
    Message message;

    @Expose
    String body;

    public SocketMessage(Type type, Message message) {
        this.type = type;
        this.message = message;
    }

    public SocketMessage(Type type, Message message, String body) {
        this.type = type;
        this.message = message;
        this.body = body;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static SocketMessage fromJson(String json) {
        return new Gson().fromJson(json, SocketMessage.class);
    }

}