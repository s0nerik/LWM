package com.lwm.app.websocket;

public class SocketMessageUtils {

    public static String getOkResponseMessage(String command) {
        return String.format(SocketMessage.FORMAT_OK, command);
    }

    public static String getErrorResponseMessage(String command) {
        return String.format(SocketMessage.FORMAT_ERROR, command);
    }

}
