package app.events.server

import groovy.transform.CompileStatic;
import org.java_websocket.WebSocket;

@CompileStatic
public class ClientReadyEvent {

    WebSocket client;

    public ClientReadyEvent(WebSocket client) {
        this.client = client;
    }
}
