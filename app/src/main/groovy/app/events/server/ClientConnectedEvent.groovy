package app.events.server;

import app.websocket.entities.ClientInfo
import groovy.transform.CompileStatic;

@CompileStatic
public class ClientConnectedEvent {

    ClientInfo clientInfo;

    public ClientConnectedEvent(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }
}
