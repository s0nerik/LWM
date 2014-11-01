package com.lwm.app.ui.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.base.DaggerActivity;
import com.lwm.app.websocket.entities.ClientInfo;

public abstract class PlaybackActivity extends DaggerActivity {

    protected MediaPlayer player;
    protected ActionBar actionBar;

    protected MenuItem broadcastButton;

    protected Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils = new Utils(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initActionBar();
    }

    protected void onClientConnected(ClientInfo info) {
        Croutons.clientConnected(this, info, R.id.albumArtLayout).show();
    }

    protected void onClientDisconnected(ClientInfo info) {
        Croutons.clientDisconnected(this, info, R.id.albumArtLayout).show();
    }

    protected void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        actionBar.setCustomView(inflater.inflate(R.layout.actionbar_listen, null));
    }

}