package com.lwm.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.fragment.PlaybackFragment;
import com.lwm.app.model.StreamPlayer;
import com.lwm.app.service.MusicService;

public class RemotePlaybackActivity extends PlaybackActivity {

    private StreamPlayer player;

    private static class CurrentSongInfo{
        static int duration;
        static String durationString;
        static String title;
        static String artist;
        static String album;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        player = MusicService.getCurrentStreamPlayer();
        setSongInfo();
    }

    @Override
    protected void onSongChanged(Intent i) {
        Bundle extras = i.getExtras();
        if(extras != null){
            CurrentSongInfo.title = extras.getString("title");
            CurrentSongInfo.album = extras.getString("album");
            CurrentSongInfo.artist = extras.getString("artist");
            CurrentSongInfo.duration = extras.getInt("duration");
            CurrentSongInfo.durationString = extras.getString("duration_string");
        }

    }

    @Override
    public void onControlButtonClicked(View v) {

    }

    @Override
    protected void setSongInfo() {
        playbackFragment.setDuration(CurrentSongInfo.durationString);
        initSeekBarUpdater(player, CurrentSongInfo.duration);
        actionBar.setTitle(CurrentSongInfo.title);
        actionBar.setSubtitle(CurrentSongInfo.artist);
        playbackFragment.setRemoteAlbumArt();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(App.TAG, "RemotePlaybackActivity.onCreate()");
        setContentView(R.layout.activity_remote_playback);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
                CurrentSongInfo.title = extras.getString("title");
                CurrentSongInfo.album = extras.getString("album");
                CurrentSongInfo.artist = extras.getString("artist");
                CurrentSongInfo.duration = extras.getInt("duration");
                CurrentSongInfo.durationString = extras.getString("duration_string");
            }
        }
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

}