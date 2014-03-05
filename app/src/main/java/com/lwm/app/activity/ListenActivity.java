package com.lwm.app.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.fragment.PlaybackFragment;
import com.lwm.app.model.StreamPlayer;
import com.lwm.app.service.MusicService;
import com.lwm.app.task.SeekBarUpdateTask;

import java.util.Timer;

public class ListenActivity extends ActionBarActivity {

    PlaybackFragment playbackFragment;
    ActionBar actionBar;

    Timer seekBarUpdateTimer = new Timer();
    int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(App.TAG, "ListenActivity.onCreate()");
        setContentView(R.layout.activity_listen);
        initActionBar();

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
                playbackFragment.setDuration(extras.getString("duration_string"));
                actionBar.setTitle(extras.getString("title"));
                actionBar.setSubtitle(extras.getString("artist"));
                duration = extras.getInt("duration");
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        StreamPlayer streamPlayer = MusicService.getCurrentStreamPlayer();
        seekBarUpdateTimer.schedule(new SeekBarUpdateTask(playbackFragment, streamPlayer, duration), 0, PlaybackFragment.SEEK_BAR_UPDATE_INTERVAL);
    }

    private void initActionBar(){
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        actionBar.setIcon(R.drawable.ic_playback_activity);
        actionBar.setDisplayHomeAsUpEnabled(true);
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