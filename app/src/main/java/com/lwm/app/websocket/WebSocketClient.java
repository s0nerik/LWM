package com.lwm.app.websocket;

import org.java_websocket.WebSocket;

public class WebSocketClient {

    private WebSocket socket;
    private String name;

    public WebSocketClient(WebSocket socket) {
        this.socket = socket;
    }

    public WebSocketClient(WebSocket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public WebSocket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
