package com.lwm.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.receiver.AbortingNotificationIntentReceiver;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.fragment.NowPlayingFragment;
import com.lwm.app.ui.notification.NowPlayingNotification;

public class BasicActivity extends ActionBarActivity {

    protected LocalPlayerService player;

    private BroadcastReceiver notificationIntentReceiver = new AbortingNotificationIntentReceiver();

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

    private void toggleNowPlayingBar(){
        if (App.localPlayerActive() && App.getLocalPlayerService().hasCurrentSong()) {
            showNowPlayingBar(true);
        } else {
            showNowPlayingBar(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (App.localPlayerActive()) {
            player = App.getLocalPlayerService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(notificationIntentReceiver, new IntentFilter(NowPlayingNotification.ACTION_SHOW));

        toggleNowPlayingBar();

        NowPlayingNotification.hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationIntentReceiver);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);
            }
        }, 1000);

    }

    public void showNowPlayingBar(boolean show){

        Log.d(App.TAG, "showNowPlayingBar()");

        FragmentManager fragmentManager = getSupportFragmentManager();
        NowPlayingFragment nowPlaying = (NowPlayingFragment) fragmentManager.findFragmentById(R.id.fragment_now_playing);

        if(show) {
            fragmentManager.beginTransaction()
                    .show(nowPlaying)
                    .commitAllowingStateLoss();

            nowPlaying.setCurrentSongInfo();
        } else {
            fragmentManager.beginTransaction()
                    .hide(nowPlaying)
                    .commitAllowingStateLoss();
        }
    }

}
