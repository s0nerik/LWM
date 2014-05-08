package com.lwm.app.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.MediaButtonIntentReceiver;
import com.lwm.app.R;
import com.lwm.app.model.Song;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.task.SeekBarUpdateTask;
import com.lwm.app.ui.fragment.PlaybackFragment;
import com.lwm.app.ui.notification.NowPlayingNotification;

import java.util.Timer;

public abstract class PlaybackActivity extends ActionBarActivity {

    protected PlaybackFragment playbackFragment;
    protected MediaPlayer player;
    protected ActionBar actionBar;

    protected MenuItem broadcastButton;

    protected long currentAlbumId;

    protected Timer seekBarUpdateTimer = new Timer();

    private AudioManager audioManager;
    private ComponentName mediaButtonIntentReceiver;

    public abstract void onControlButtonClicked(View v);
    protected abstract void setSongInfo(Song song);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        initActionBar();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mediaButtonIntentReceiver = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());

        audioManager.registerMediaButtonEventReceiver(mediaButtonIntentReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioManager.unregisterMediaButtonEventReceiver(mediaButtonIntentReceiver);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
        NowPlayingNotification.hide();
    }

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