package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Interpolator;

import com.lwm.app.App;
import com.lwm.app.R;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

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

    private AnimatorSet settingsBtnAnimation;
    private AnimatorSet localBtnAnimation;
    private AnimatorSet remoteBtnAnimation;
    private View localBtn;
    private View localBtnShadow;
    private View localBtnText;
    private View remoteBtn;
    private View remoteBtnShadow;
    private View remoteBtnText;
    private View settingsBtn;
    private View settingsBtnShadow;
    private View settingsBtnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.nothing, R.anim.nothing);
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("always_local", false)){
            startActivity(new Intent(this, LocalSongChooserActivity.class));
        }
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.actionbar_main_activity);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        localBtn = findViewById(R.id.btn_local);
        localBtn.setOnClickListener(localMusicButtonClickListener);

        remoteBtn = findViewById(R.id.btn_remote);
        remoteBtn.setOnClickListener(remoteMusicButtonClickListener);

        settingsBtn = findViewById(R.id.btn_settings);
        settingsBtn.setOnClickListener(settingsButtonClickListener);

        localBtnShadow = findViewById(R.id.shadow_local_btn);
        remoteBtnShadow = findViewById(R.id.shadow_remote_btn);
        settingsBtnShadow = findViewById(R.id.shadow_settings_btn);

        localBtnText = findViewById(R.id.btn_local_text);
        remoteBtnText = findViewById(R.id.btn_remote_text);
        settingsBtnText = findViewById(R.id.btn_settings_text);

        localBtnAnimation = new AnimatorSet();
        localBtnAnimation.playTogether(
                ObjectAnimator.ofFloat(localBtn, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(localBtnShadow, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(localBtnText, "alpha", 0f, 1f)
        );

        remoteBtnAnimation = new AnimatorSet();
        remoteBtnAnimation.playTogether(
                ObjectAnimator.ofFloat(remoteBtn, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(remoteBtnShadow, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(remoteBtnText, "alpha", 0f, 1f)
        );

        settingsBtnAnimation = new AnimatorSet();
        settingsBtnAnimation.playTogether(
                ObjectAnimator.ofFloat(settingsBtn, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(settingsBtnShadow, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(settingsBtnText, "alpha", 0f, 1f)
        );

//        // Hide everything before animation it
//        localBtn.setVisibility(View.INVISIBLE);
//        localBtnShadow.setVisibility(View.INVISIBLE);
//        localBtnText.setVisibility(View.INVISIBLE);
//        remoteBtn.setVisibility(View.INVISIBLE);
//        remoteBtnShadow.setVisibility(View.INVISIBLE);
//        remoteBtnText.setVisibility(View.INVISIBLE);
//        settingsBtn.setVisibility(View.INVISIBLE);
//        settingsBtnShadow.setVisibility(View.INVISIBLE);
//        settingsBtnText.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show everything before animating it
                localBtn.setVisibility(View.VISIBLE);
                localBtnShadow.setVisibility(View.VISIBLE);
                localBtnText.setVisibility(View.VISIBLE);
                remoteBtn.setVisibility(View.VISIBLE);
                remoteBtnShadow.setVisibility(View.VISIBLE);
                remoteBtnText.setVisibility(View.VISIBLE);
                settingsBtn.setVisibility(View.VISIBLE);
                settingsBtnShadow.setVisibility(View.VISIBLE);
                settingsBtnText.setVisibility(View.VISIBLE);

                // Actually animate it
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        localBtnAnimation.setDuration(200),
                        remoteBtnAnimation.setDuration(400),
                        settingsBtnAnimation.setDuration(800)
                );
                set.start();
            }
        }, 500);

    }

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

    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float paramFloat) {
            return Math.abs(paramFloat -1f);
        }
    }

}
