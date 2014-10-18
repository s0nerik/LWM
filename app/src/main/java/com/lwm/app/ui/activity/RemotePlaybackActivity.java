package com.lwm.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.events.player.PlaybackPausedEvent;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.events.server.StopWebSocketClientEvent;
import com.lwm.app.model.Song;
import com.lwm.app.service.StreamPlayerService;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.remote_playback, menu);
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