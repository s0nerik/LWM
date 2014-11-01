package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.events.access_point.AccessPointStateChangingEvent;
import com.lwm.app.events.access_point.StartServerEvent;
import com.lwm.app.events.access_point.StopServerEvent;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.SetUnreadMessagesEvent;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiApManager;
import com.lwm.app.ui.Croutons;
import com.squareup.otto.Subscribe;

public class LocalPlaybackActivity extends PlaybackActivity {

    private MenuItem chatButton;
    private TextView newMessagesCounter;
    private int unreadMessagesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_broadcast:
                WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiAP wifiAP = new WifiAP();
                wifiAP.toggleWiFiAP(wm, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setMenuProgressIndicator(boolean show){

        if(broadcastButton == null) return;

        if(show)
            MenuItemCompat.setActionView(broadcastButton, R.layout.progress_actionbar_broadcast);
        else
            MenuItemCompat.setActionView(broadcastButton, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.local_playback, menu);

        broadcastButton = menu.findItem(R.id.action_broadcast);
        chatButton = menu.findItem(R.id.action_chat);

        View v = MenuItemCompat.getActionView(chatButton);
        newMessagesCounter = (TextView) v.findViewById(R.id.newMessagesCounter);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LocalPlaybackActivity.this, ChatActivity.class));
            }
        });

        WifiApManager manager = new WifiApManager(this);

        setBroadcastButtonState(manager.isWifiApEnabled());

        return true;
    }

    @Subscribe
    public void accessPointStateChanging(AccessPointStateChangingEvent event) {
        setMenuProgressIndicator(true);
    }

    @Subscribe
    public void accessPointEnabled(StartServerEvent event) {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(true);
    }

    @Subscribe
    public void accessPointDisabled(StopServerEvent event) {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(false);
    }

    @Subscribe
    public void onClientConnected(ClientConnectedEvent event) {
        onClientConnected(event.getClientInfo());
    }

    @Subscribe
    public void onClientDisconnected(ClientDisconnectedEvent event) {
        onClientDisconnected(event.getClientInfo());
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Croutons.messageReceived(this, event.getMessage(), R.id.albumArtLayout).show();
        unreadMessagesCount += 1;
        newMessagesCounter.setVisibility(View.VISIBLE);
        newMessagesCounter.setText(String.valueOf(unreadMessagesCount < 10? unreadMessagesCount : "+"));
    }

    @Subscribe
    public void setUnreadMessagesCount(SetUnreadMessagesEvent event) {
        unreadMessagesCount = event.getCount();
        if (newMessagesCounter != null) {
            if (unreadMessagesCount > 0) {
                newMessagesCounter.setVisibility(View.VISIBLE);
                newMessagesCounter.setText(String.valueOf(unreadMessagesCount < 10 ? unreadMessagesCount : "+"));
            } else {
                newMessagesCounter.setVisibility(View.GONE);
            }
        }
    }

    private void setBroadcastButtonState(boolean broadcasting) {
        if (broadcastButton != null) if (broadcasting) {
            broadcastButton.setIcon(R.drawable.ic_action_broadcast_active);
            chatButton.setVisible(true);
        } else {
            broadcastButton.setIcon(R.drawable.ic_action_broadcast);
            chatButton.setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}