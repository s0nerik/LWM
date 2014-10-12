package com.lwm.app.websocket;

public class SocketMessageUtils {

    public static String getOkResponseMessage(SocketMessage message) {
        return SocketMessage.formatWithMessage(SocketMessage.OK, message);
    }

    public static String getErrorResponseMessage(SocketMessage message) {
        return SocketMessage.formatWithMessage(SocketMessage.ERROR, message);
    }

}
