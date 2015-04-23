package com.lwm.app.ui.activity;

import android.os.Bundle;

import com.lwm.app.Utils;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.base.DaggerActivity;

import javax.inject.Inject;

public abstract class PlaybackActivity extends DaggerActivity {

    @Inject
    protected Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onClientConnected(ClientConnectedEvent event) {
        Croutons.clientConnected(this, event.getClientInfo()).show();
    }

    protected void onClientDisconnected(ClientDisconnectedEvent event) {
        Croutons.clientDisconnected(this, event.getClientInfo()).show();
    }

}