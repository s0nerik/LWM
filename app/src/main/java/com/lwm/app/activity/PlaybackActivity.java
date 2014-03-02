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
    int duration;

    Timer seekBarUpdateTimer = new Timer();

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {

            String action = i.getAction();

            switch(action){

                case MusicPlayer.SONG_CHANGED:
                    player = MusicService.getCurrentPlayer();

                    // Now playing fragment changes
                    playbackFragment.setTitle(player.getCurrentTitle());
                    playbackFragment.setArtist(player.getCurrentArtist());
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
        initActionBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        player = MusicService.getCurrentPlayer();
        duration = player.getDuration();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);

        playbackFragment.setPlayButton(player.isPlaying());
        playbackFragment.setTitle(player.getCurrentTitle());
        playbackFragment.setArtist(player.getCurrentArtist());
        playbackFragment.setDuration(player.getCurrentDurationInMinutes());
        playbackFragment.setAlbumArtFromUri(player.getCurrentAlbumArtUri());
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        actionBar.setTitle(R.string.now_playing);
        actionBar.hide();
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

//    public void onNextButtonClicked(View v){
//        startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_NEXT_SONG));
//    }
//
//    public void onPrevButtonClicked(View v){
//        startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_PREV_SONG));
//    }
//
//    public void onPauseButtonClicked(View v){
//        if(MusicService.getCurrentPlayer().isPlaying()){
//            startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_PAUSE_SONG));
//        }else{
//            startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_UNPAUSE_SONG));
//        }
//    }

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

}