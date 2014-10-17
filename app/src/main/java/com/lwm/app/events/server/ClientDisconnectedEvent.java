package com.lwm.app.events.server;

public class ClientDisconnectedEvent {

    private String name;

    public ClientDisconnectedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
