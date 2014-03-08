package com.lwm.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.model.LocalPlayer;
import com.lwm.app.service.MusicService;

public class LocalPlaybackActivity extends PlaybackActivity {

    private LocalPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        player = MusicService.getCurrentLocalPlayer();
        setSongInfo();
    }

    @Override
    public void onControlButtonClicked(View v){
        switch(v.getId()){
            case R.id.fragment_playback_next:
                startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_NEXT_SONG));
                break;

            case R.id.fragment_playback_prev:
                startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_PREV_SONG));
                break;

            case R.id.fragment_playback_play_pause:
                if(player.isPlaying()){
                    startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_PAUSE_SONG));
                }else{
                    startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_UNPAUSE_SONG));
                }
                break;

            case R.id.fragment_playback_shuffle_button:
                break;

            case R.id.fragment_playback_repeat_button:
                break;
        }
    }

    @Override
    protected void onSongChanged(Intent i) {
        player = MusicService.getCurrentLocalPlayer();
        initSeekBarUpdater(player);
        setSongInfo();
    }

    @Override
    protected void setSongInfo() {
        actionBar.setTitle(player.getCurrentTitle());
        actionBar.setSubtitle(player.getCurrentArtist());

        playbackFragment.setDuration(player.getCurrentDurationInMinutes());

        initSeekBarUpdater(player);

        // Now playing fragment changes
        playbackFragment.setDuration(player.getCurrentDurationInMinutes());
        playbackFragment.setPlayButton(player.isPlaying());
        playbackFragment.setCurrentAlbumArt();

        // Change background if album art has changed
        int newAlbumId = player.getCurrentAlbumId();
        if(newAlbumId != currentAlbumId){
            playbackFragment.setBackgroundImageUri(player.getCurrentAlbumArtUri());
            currentAlbumId = newAlbumId;
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