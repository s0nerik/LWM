package com.lwm.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.SetUnreadMessagesEvent;
import com.lwm.app.events.player.PlaybackPausedEvent;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.events.server.StopWebSocketClientEvent;
import com.lwm.app.model.Song;
import com.lwm.app.service.StreamPlayerService;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.fragment.RemotePlaybackFragment;
import com.squareup.otto.Subscribe;

public class RemotePlaybackActivity extends PlaybackActivity {

    private StreamPlayerService player;

    private int duration;
    private String durationString;
    private String title;
    private String artist;
    private String album;
    private RemotePlaybackFragment playbackFragment;

    private int unreadMessagesCount = 0;
    private MenuItem chatButton;
    private TextView newMessagesCounter;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        playbackFragment = (RemotePlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);

        player = App.getStreamPlayerService();
    }

    @Override
    public void onControlButtonClicked(View v) {

    }

    @Override
    protected void setSongInfo(Song song) {
        if(song != null) {
            playbackFragment.showWaitingFrame(false);

            View v = actionBar.getCustomView();
            TextView title = (TextView) v.findViewById(R.id.title);
            TextView subtitle = (TextView) v.findViewById(R.id.subtitle);
            title.setText(song.getTitle());
            subtitle.setText(song.getArtist());

            durationString = song.getDurationString();
            playbackFragment.setDuration(durationString);
            duration = song.getDuration();
            initSeekBarUpdater(player.getPlayer(), duration);
            playbackFragment.setRemoteAlbumArt();
        }else{
            playbackFragment.showWaitingFrame(true);
        }
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
        App.getBus().register(this);
        Song song = App.getStreamPlayerService().getCurrentSong();
        if (song != null) {
            setSongInfo(song);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getBus().unregister(this);
    }

    @Subscribe
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        Log.d(App.TAG, "Playback started (RemotePlaybackActivity)");
        setSongInfo(event.getSong());
        playbackFragment.setPlayButton(true);
    }

    @Subscribe
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        playbackFragment.setPlayButton(false);
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Croutons.messageReceived(this, event.getMessage(), R.id.offsetted_albumart).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.remote_playback, menu);

        chatButton = menu.findItem(R.id.action_chat);
        View v = MenuItemCompat.getActionView(chatButton);
        newMessagesCounter = (TextView) v.findViewById(R.id.newMessagesCounter);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RemotePlaybackActivity.this, ChatActivity.class));
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_chat:
                startActivity(new Intent(this, ChatActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(player.isPlaying()) {
            player.stop();
        }
        App.getBus().post(new StopWebSocketClientEvent());
        super.onDestroy();
    }
}