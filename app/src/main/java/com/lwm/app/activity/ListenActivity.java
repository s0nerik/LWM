package com.lwm.app.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.fragment.PlaybackFragment;

public class ListenActivity extends ActionBarActivity {

    PlaybackFragment playbackFragment;

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
                playbackFragment.setDuration(extras.getString("duration"));
            }
        }
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        actionBar.setTitle(R.string.now_playing);
    }

}