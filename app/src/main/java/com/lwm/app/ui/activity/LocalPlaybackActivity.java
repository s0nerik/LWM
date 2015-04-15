package com.lwm.app.ui.activity;

import android.os.Bundle;

import com.lwm.app.R;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.squareup.otto.Subscribe;

public class LocalPlaybackActivity extends PlaybackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

    @Subscribe
    @Override
    protected void onClientConnected(ClientConnectedEvent event) {
        super.onClientConnected(event);
    }

    @Subscribe
    @Override
    protected void onClientDisconnected(ClientDisconnectedEvent event) {
        super.onClientDisconnected(event);
    }
}