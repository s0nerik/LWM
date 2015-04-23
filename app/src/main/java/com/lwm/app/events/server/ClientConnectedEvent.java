package com.lwm.app.events.server;

import com.lwm.app.websocket.entities.ClientInfo;

public class ClientConnectedEvent {

    private ClientInfo clientInfo;

    public ClientConnectedEvent(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
