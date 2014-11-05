package com.lwm.app.ui.activity;

import android.os.Bundle;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.base.DaggerActivity;
import com.lwm.app.websocket.entities.ClientInfo;

import javax.inject.Inject;

public abstract class PlaybackActivity extends DaggerActivity {

    @Inject
    protected Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onClientConnected(ClientInfo info) {
        Croutons.clientConnected(this, info, R.id.albumArtLayout).show();
    }

    protected void onClientDisconnected(ClientInfo info) {
        Croutons.clientDisconnected(this, info, R.id.albumArtLayout).show();
    }

}