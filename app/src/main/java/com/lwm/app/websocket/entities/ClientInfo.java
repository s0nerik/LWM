package com.lwm.app.websocket.entities;

import com.google.gson.annotations.Expose;

public class ClientInfo {

    @Expose
    private String name;

    public ClientInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
