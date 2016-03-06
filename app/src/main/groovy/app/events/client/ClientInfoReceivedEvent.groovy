package app.events.client;

import app.websocket.entities.ClientInfo
import groovy.transform.CompileStatic;
import org.java_websocket.WebSocket;

@CompileStatic
public class ClientInfoReceivedEvent {

    WebSocket webSocket;
    ClientInfo clientInfo;

    public ClientInfoReceivedEvent(WebSocket webSocket, ClientInfo clientInfo) {
        this.webSocket = webSocket;
        this.clientInfo = clientInfo;
    }
}
