package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.event.player.PlaybackPausedEvent;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.ui.fragment.RemotePlaybackFragment;
import com.squareup.otto.Subscribe;

public class RemotePlaybackActivity extends PlaybackActivity {

    private StreamPlayer player;

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

        player = App.getMusicService().getStreamPlayer();
        if(savedInstanceState == null) {
            player.playFromCurrentPosition();
        }
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
            initSeekBarUpdater(player, duration);
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
        App.getEventBus().register(this);
        Song song = StreamPlayer.getCurrentSong();
        if (song != null) {
            setSongInfo(StreamPlayer.getCurrentSong());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getEventBus().unregister(this);
    }

    @Subscribe
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        setSongInfo(event.getSong());
        playbackFragment.setPlayButton(true);
    }

    @Subscribe
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        playbackFragment.setPlayButton(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(player.isPlaying()) {
            player.stop();
        }
        player.detachFromStation();
    }

}