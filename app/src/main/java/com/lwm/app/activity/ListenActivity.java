package com.lwm.app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.service.MusicService;

public class ListenActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(App.TAG, "ListenActivity.onCreate()");
        setContentView(R.layout.activity_listen);
        initActionBar();
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY_STREAM);
        startService(intent);
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        actionBar.setTitle(R.string.now_playing);
    }

}