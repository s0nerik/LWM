package app.ui.activity;

import android.os.Bundle;

import app.Utils;
import app.events.server.ClientConnectedEvent;
import app.events.server.ClientDisconnectedEvent;
import app.ui.Croutons;
import app.ui.base.DaggerActivity
import groovy.transform.CompileStatic;

import javax.inject.Inject;

@CompileStatic
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