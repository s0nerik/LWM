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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.caverock.androidsvg.SVGImageView;
import com.lwm.app.App;
import com.lwm.app.R;

public class MainActivity extends ActionBarActivity {

    private SVGImageView localMusicButtonIcon;
    private SVGImageView remoteMusicButtonIcon;

    private View.OnTouchListener localMusicButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    localMusicButtonIcon.setImageAsset("icons/local_music_glow.svg");
                    Log.d(App.TAG, "MotionEvent.ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(App.TAG, "MotionEvent.ACTION_UP");
                    localMusicButtonIcon.setImageAsset("icons/local_music_.svg");
                    break;
            }
            return false;
        }
    };

    private View.OnTouchListener remoteMusicButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    remoteMusicButtonIcon.setImageAsset("icons/radio_glow.svg");
                    Log.d(App.TAG, "MotionEvent.ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(App.TAG, "MotionEvent.ACTION_UP");
                    remoteMusicButtonIcon.setImageAsset("icons/radio_.svg");
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener localMusicButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(App.TAG, "onClick");
            localMusicButtonIcon.setImageAsset("icons/local_music_glow.svg");
            startActivity(new Intent(MainActivity.this, LocalSongChooserActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
        }
    };

    private View.OnClickListener remoteMusicButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(App.TAG, "onClick");
            remoteMusicButtonIcon.setImageAsset("icons/radio_glow.svg");
            startActivity(new Intent(MainActivity.this, StationChooserActivity.class));
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

        localMusicButtonIcon = (SVGImageView) findViewById(R.id.start_screen_local_music_button_icon);
        remoteMusicButtonIcon = (SVGImageView) findViewById(R.id.start_screen_remote_music_button_icon);

        View localMusicButton = findViewById(R.id.start_screen_local_music_button);
        localMusicButton.setOnTouchListener(localMusicButtonTouchListener);
        localMusicButton.setOnClickListener(localMusicButtonClickListener);

        View remoteMusicButton = findViewById(R.id.start_screen_remote_music_button);
        remoteMusicButton.setOnTouchListener(remoteMusicButtonTouchListener);
        remoteMusicButton.setOnClickListener(remoteMusicButtonClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        localMusicButtonIcon.setImageAsset("icons/local_music_.svg");
        remoteMusicButtonIcon.setImageAsset("icons/radio_.svg");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    public void onPlayerTypeChosen(View v){
//        Log.d(App.TAG, "onPlayerTypeChosen");
//        Intent intent;
//        switch(v.getId()) {
//            case R.id.start_screen_local_music_button:
//                intent = new Intent(this, LocalSongChooserActivity.class);
//                break;
//            case R.id.start_screen_remote_music_button:
//                intent = new Intent(this, StationChooserActivity.class);
//                break;
//            default:
//                return;
//        }
//        startActivity(intent);
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
