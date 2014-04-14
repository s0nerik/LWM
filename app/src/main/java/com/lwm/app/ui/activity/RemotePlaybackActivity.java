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
import com.lwm.app.ui.fragment.PlaybackFragment;

public class RemotePlaybackActivity extends PlaybackActivity implements PlayerListener {

    private StreamPlayer player;

    private int duration;
    private String durationString;
    private String title;
    private String artist;
    private String album;
    private PlaybackFragment playbackFragment;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
        if(App.isMusicServiceBound()){
            player = App.getMusicService().getStreamPlayer();
//            setSongInfo();
        }
    }

//    @Override
//    protected void onSongChanged(Intent i) {
//        Bundle extras = i.getExtras();
//        if(extras != null){
//            title = extras.getString("title");
//            album = extras.getString("album");
//            artist = extras.getString("artist");
//            duration = extras.getInt("duration");
//            durationString = extras.getString("duration_string");
//        }
//
//    }

    @Override
    public void onControlButtonClicked(View v) {

    }

    @Override
    protected void setSongInfo(Song song) {
        durationString = song.getDurationString();
        playbackFragment.setDuration(durationString);
        duration = song.getDuration();
        initSeekBarUpdater(player, duration);
        title = song.getTitle();
        actionBar.setTitle(title);
        artist = song.getArtist();
        actionBar.setSubtitle(artist);
        playbackFragment.setRemoteAlbumArt();
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
        playbackFragment.setPlayButton(true);
    }
}