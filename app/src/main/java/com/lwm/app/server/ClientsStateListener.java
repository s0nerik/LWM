package com.lwm.app.server;

public interface ClientsStateListener {
    public void onClientsReady();
    public void onWaitClients();
}
