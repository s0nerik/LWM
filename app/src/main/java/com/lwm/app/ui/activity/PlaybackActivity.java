package com.lwm.app.ui.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.task.SeekBarUpdateTask;
import com.lwm.app.ui.fragment.PlaybackFragment;

import java.util.Timer;

public abstract class PlaybackActivity extends ActionBarActivity {

    protected PlaybackFragment playbackFragment;
    protected MediaPlayer player;
    protected ActionBar actionBar;

    protected MenuItem broadcastButton;

    protected long currentAlbumId;

    protected Timer seekBarUpdateTimer = new Timer();

//    protected BroadcastReceiver onBroadcast = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent i) {
//            String action = i.getAction();
//            switch(action){
//                case BasePlayer.SONG_CHANGED:
//                    onSongChanged(i);
//                    break;
//
////                case BasePlayer.PLAYBACK_PAUSED:
////                    playbackFragment.setPlayButton(false);
////                    break;
////
////                case BasePlayer.PLAYBACK_STARTED:
////                    playbackFragment.setPlayButton(true);
////                    break;
//            }
//
//        }
//    };

//    protected abstract void onSongChanged(Intent i);
    public abstract void onControlButtonClicked(View v);
    protected abstract void setSongInfo(Song song);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        initActionBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerReceiver(onBroadcast, new IntentFilter(BasePlayer.SONG_CHANGED));
////        registerReceiver(onBroadcast, new IntentFilter(BasePlayer.PLAYBACK_PAUSED));
////        registerReceiver(onBroadcast, new IntentFilter(BasePlayer.PLAYBACK_STARTED));
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(onBroadcast);
//    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelSeekBarUpdater();
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

    protected void initActionBar(){
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#99000000")));
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        actionBar.setIcon(R.drawable.ic_playback_activity);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        actionBar.setCustomView(inflater.inflate(R.layout.actionbar_listen, null));
    }

    protected void initSeekBarUpdater(BasePlayer player){
        seekBarUpdateTimer.cancel();
        seekBarUpdateTimer.purge();
        seekBarUpdateTimer = new Timer();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(playbackFragment, player, player.getDuration()), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
    }

    protected void initSeekBarUpdater(BasePlayer player, int duration){
        seekBarUpdateTimer.cancel();
        seekBarUpdateTimer.purge();
        seekBarUpdateTimer = new Timer();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(playbackFragment, player, duration), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
    }

    protected void cancelSeekBarUpdater(){
        seekBarUpdateTimer.cancel();
        seekBarUpdateTimer.purge();
    }

}