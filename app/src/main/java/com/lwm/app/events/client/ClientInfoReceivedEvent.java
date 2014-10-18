package com.lwm.app.events.client;

import com.lwm.app.websocket.entities.ClientInfo;

import org.java_websocket.WebSocket;

public class ClientInfoReceivedEvent {

    private WebSocket webSocket;
    private ClientInfo clientInfo;

    public ClientInfoReceivedEvent(WebSocket webSocket, ClientInfo clientInfo) {
        this.webSocket = webSocket;
        this.clientInfo = clientInfo;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }
}
