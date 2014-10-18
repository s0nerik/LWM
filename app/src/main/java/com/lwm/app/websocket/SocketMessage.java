package com.lwm.app.websocket;

public enum SocketMessage {
    START_FROM, SEEK_TO,
    CURRENT_POSITION, IS_PLAYING,
    OK, ERROR,
    PAUSE, UNPAUSE, START,
    PREPARE, READY,
    MESSAGE, CLIENT_INFO;

    public static String getStringToSend(SocketMessage message) {
        return message.name();
    }

    public static String formatWithMessage(SocketMessage message, SocketMessage otherMessage) {
        return withNewLine(message) + otherMessage.name();
    }

    public static String formatWithString(SocketMessage message, String s) {
        return withNewLine(message) + s;
    }

    public static String formatWithInt(SocketMessage message, int i) {
        return withNewLine(message) + i;
    }

    public static String formatWithBoolean(SocketMessage message, boolean b) {
        return withNewLine(message) + b;
    }

    private static String withNewLine(SocketMessage message) {
        return message.name() + "\n";
    }

}