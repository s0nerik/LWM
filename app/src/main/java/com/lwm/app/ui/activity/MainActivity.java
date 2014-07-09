package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;

public class MainActivity extends ActionBarActivity {

    private View.OnClickListener localMusicButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(App.TAG, "onClick");
            startActivity(new Intent(MainActivity.this, LocalSongChooserActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
        }
    };

    private View.OnClickListener remoteMusicButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(App.TAG, "onClick");
            startActivity(new Intent(MainActivity.this, StationChooserActivity.class));
        }
    };

    private View.OnClickListener settingsButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("always_local", false)){
            startActivity(new Intent(this, LocalSongChooserActivity.class));
        }
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_main_activity);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        findViewById(R.id.btn_local).setOnClickListener(localMusicButtonClickListener);
        findViewById(R.id.btn_remote).setOnClickListener(remoteMusicButtonClickListener);
        findViewById(R.id.btn_settings).setOnClickListener(settingsButtonClickListener);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.start_screen, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(this, PreferenceActivity.class);
//            startActivity(intent);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

}
