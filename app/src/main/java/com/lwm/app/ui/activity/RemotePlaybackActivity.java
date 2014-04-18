package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.model.Song;
import com.lwm.app.player.PlayerListener;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.ui.fragment.RemotePlaybackFragment;

public class RemotePlaybackActivity extends PlaybackActivity implements PlayerListener {

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
            player.registerListener(this);
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

            durationString = song.getDurationString();
            playbackFragment.setDuration(durationString);
            duration = song.getDuration();
            initSeekBarUpdater(player, duration);
            title = song.getTitle();
            actionBar.setTitle(title);
            artist = song.getArtist();
            actionBar.setSubtitle(artist);
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
        player.registerListener(this);
        Song song = StreamPlayer.getCurrentSong();
        if (song != null) {
            setSongInfo(StreamPlayer.getCurrentSong());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.unregisterListener();
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
    public void onSongChanged(Song song) {
        setSongInfo(song);
    }

    @Override
    public void onPlaybackPaused() {
        playbackFragment.setPlayButton(false);
    }

    @Override
    public void onPlaybackStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playbackFragment.setPlayButton(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(player.isPlaying()) {
            player.stop();
        }
    }

}