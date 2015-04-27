package app.events.server;

import app.websocket.entities.ClientInfo;

public class ClientDisconnectedEvent {

    private ClientInfo clientInfo;

    public ClientDisconnectedEvent(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
