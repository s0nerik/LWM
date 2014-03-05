package com.lwm.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.fragment.PlaybackFragment;
import com.lwm.app.model.MusicPlayer;
import com.lwm.app.service.MusicService;

import java.util.Timer;
import java.util.TimerTask;

public class PlaybackActivity extends ActionBarActivity {

    PlaybackFragment playbackFragment;
    MusicPlayer player;
    ActionBar actionBar;
    int currentAlbumId;
    int duration;

    Timer seekBarUpdateTimer = new Timer();

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            String action = i.getAction();
            switch(action){
                case MusicPlayer.SONG_CHANGED:
                    player = MusicService.getCurrentPlayer();

                    actionBar.setTitle(player.getCurrentTitle());
                    actionBar.setSubtitle(player.getCurrentArtist());

                    // Now playing fragment changes
                    playbackFragment.setDuration(player.getCurrentDurationInMinutes());

                    duration = player.getDuration();

                    seekBarUpdateTimer.cancel();
                    seekBarUpdateTimer.purge();
                    seekBarUpdateTimer = new Timer();
                    seekBarUpdateTimer.schedule(new SeekBarUpdateTask(), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
                    playbackFragment.setPlayButton(true);

                    if(i.hasExtra(MusicPlayer.ALBUM_ART_URI)){
                        String gotUri = i.getStringExtra(MusicPlayer.ALBUM_ART_URI);
                        playbackFragment.setAlbumArtFromUri(Uri.parse(gotUri));
                    }else{
                        playbackFragment.setDefaultAlbumArt();
                    }

                    int newAlbumId = player.getCurrentAlbumId();
                    if(newAlbumId != currentAlbumId){
                        playbackFragment.setBackgroundImageUri(player.getCurrentAlbumArtUri());
                        currentAlbumId = newAlbumId;
                    }
                    break;

                case MusicPlayer.PLAYBACK_PAUSED:
                    playbackFragment.setPlayButton(false);
                    break;

                case MusicPlayer.PLAYBACK_STARTED:
                    playbackFragment.setPlayButton(true);
                    break;

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        player = MusicService.getCurrentPlayer();
        actionBar = getSupportActionBar();
        initActionBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        duration = player.getDuration();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);

        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);

        playbackFragment.setPlayButton(player.isPlaying());
        playbackFragment.setDuration(player.getCurrentDurationInMinutes());
        playbackFragment.setAlbumArtFromUri(player.getCurrentAlbumArtUri());
        playbackFragment.setBackgroundImageUri(player.getCurrentAlbumArtUri());
        currentAlbumId = player.getCurrentAlbumId();
    }

    private void initActionBar(){
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        actionBar.setTitle(player.getCurrentTitle());
        actionBar.setSubtitle(player.getCurrentArtist());
        actionBar.setIcon(R.drawable.ic_playback_activity);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(onBroadcast, new IntentFilter(MusicPlayer.SONG_CHANGED));
        registerReceiver(onBroadcast, new IntentFilter(MusicPlayer.PLAYBACK_PAUSED));
        registerReceiver(onBroadcast, new IntentFilter(MusicPlayer.PLAYBACK_STARTED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
    }

    public void onControlButtonClicked(View v){
        switch(v.getId()){
            case R.id.fragment_playback_next:
                startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_NEXT_SONG));
                break;

            case R.id.fragment_playback_prev:
                startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_PREV_SONG));
                break;

            case R.id.fragment_playback_play_pause:
                if(MusicService.getCurrentPlayer().isPlaying()){
                    startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_PAUSE_SONG));
                }else{
                    startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_UNPAUSE_SONG));
                }
                break;
        }
    }

    private class SeekBarUpdateTask extends TimerTask {
        int progress;
        @Override
        public void run() {
            progress = (int) (player.getCurrentPosition()/(float) duration * PlaybackFragment.SEEK_BAR_MAX);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playbackFragment.setSeekBarPosition(progress);
                    playbackFragment.setCurrentTime(player.getCurrentPositionInMinutes());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
//                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}