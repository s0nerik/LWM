package com.lwm.app.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.fragment.PlaybackFragment;
import com.lwm.app.model.Song;
import com.lwm.app.player.StreamPlayer;

public class RemotePlaybackActivity extends PlaybackActivity {

    private StreamPlayer player;

    private int duration;
    private String durationString;
    private String title;
    private String artist;
    private String album;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

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
    protected void setSongInfo(Song song) {

    }

    @Override
    public void onControlButtonClicked(View v) {

    }

//    @Override
//    protected void setSongInfo() {
//        playbackFragment.setDuration(durationString);
//        initSeekBarUpdater(player, duration);
//        actionBar.setTitle(title);
//        actionBar.setSubtitle(artist);
//        playbackFragment.setRemoteAlbumArt();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(App.TAG, "RemotePlaybackActivity.onCreate()");
        setContentView(R.layout.activity_remote_playback);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
                title = extras.getString("title");
                album = extras.getString("album");
                artist = extras.getString("artist");
                duration = extras.getInt("duration");
                durationString = extras.getString("duration_string");
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