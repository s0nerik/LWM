package com.lwm.app.events.server;

public class ClientConnectedEvent {

    private String name;

    public ClientConnectedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
