package app.events.server;

import org.java_websocket.WebSocket;

public class ClientReadyEvent {

    private WebSocket client;

    public ClientReadyEvent(WebSocket client) {
        this.client = client;
    }

    public WebSocket getClient() {
        return client;
    }
}
