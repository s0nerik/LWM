package com.lwm.app.websocket;

public enum SocketMessage {
    START_FROM, SEEK_TO, CURRENT_POSITION, IS_PLAYING, OK, ERROR, PAUSE, UNPAUSE, START, PREPARE, READY, MESSAGE;

    public static String getStringToSend(SocketMessage message) {
        return message.name();
    }

    public static String formatWithMessage(SocketMessage message, SocketMessage otherMessage) {
        return withSpace(message) + otherMessage.name();
    }

    public static String formatWithString(SocketMessage message, String s) {
        return withSpace(message) + s;
    }

    public static String formatWithStringNewLine(SocketMessage message, String s) {
        return withNewLine(message) + s;
    }

    public static String formatWithInt(SocketMessage message, int i) {
        return withSpace(message) + i;
    }

    public static String formatWithBoolean(SocketMessage message, boolean b) {
        return withSpace(message) + b;
    }

    private static String withSpace(SocketMessage message) {
        return message.name() + " ";
    }

    private static String withNewLine(SocketMessage message) {
        return message.name() + "\n";
    }

}