package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.SetUnreadMessagesEvent;
import com.lwm.app.events.client.SocketClosedEvent;
import com.lwm.app.events.server.StopWebSocketClientEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.fragment.playback.RemotePlaybackFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import uk.me.lewisdeane.ldialogs.CustomDialog;

public class RemotePlaybackActivity extends PlaybackActivity {

    private int duration;
    private String durationString;
    private String title;
    private String artist;
    private String album;
    private RemotePlaybackFragment playbackFragment;

    private int unreadMessagesCount = 0;
    private MenuItem chatButton;
    private TextView newMessagesCounter;

    @Inject
    Bus bus;

    @Inject
    StreamPlayer player;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        playbackFragment = (RemotePlaybackFragment) getFragmentManager().findFragmentById(R.id.fragment_playback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(App.TAG, "RemotePlaybackActivity.onCreate()");
        setContentView(R.layout.activity_remote_playback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        Song song = player.getCurrentSong();
        if (song != null) {
//            setSongInfo(song);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Croutons.messageReceived(this, event.getMessage(), R.id.albumArtLayout).show();
        unreadMessagesCount += 1;
        newMessagesCounter.setVisibility(View.VISIBLE);
        newMessagesCounter.setText(String.valueOf(unreadMessagesCount < 10 ? unreadMessagesCount : "+"));
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

    @Subscribe
    public void onSocketClosed(SocketClosedEvent event) {
        String title = "Station stopped broadcasting";
        CustomDialog.Builder builder = new CustomDialog.Builder(this, title, "Ok");
        builder.build().setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                finish();
            }

            @Override
            public void onCancelClick() {

            }
        }).show();
    }

    @Override
    protected void onDestroy() {
        if(player.isPlaying()) {
            player.stop();
        }
        bus.post(new StopWebSocketClientEvent());
        super.onDestroy();
    }
}