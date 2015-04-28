package app.events.server;

import app.websocket.entities.ClientInfo
import groovy.transform.CompileStatic;

@CompileStatic
public class ClientDisconnectedEvent {

    ClientInfo clientInfo;

    public ClientDisconnectedEvent(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }
}
