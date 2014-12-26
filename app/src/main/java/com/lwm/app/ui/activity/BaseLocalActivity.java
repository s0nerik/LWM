package com.lwm.app.ui.activity;

import android.media.AudioManager;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.lwm.app.R;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.base.DaggerActivity;
import com.lwm.app.ui.fragment.NowPlayingFragment;
import com.lwm.app.websocket.entities.ClientInfo;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public abstract class BaseLocalActivity extends DaggerActivity {

    @Inject
    protected LocalPlayer player;

    @Inject
    protected Bus bus;

    @Inject
    AudioManager audioManager;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void toggleNowPlayingBar(){
        if (player.hasCurrentSong()) {
            showNowPlayingBar(true);
        } else {
            showNowPlayingBar(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleNowPlayingBar();
    }

    protected void onClientConnected(ClientInfo info) {
        Croutons.clientConnected(this, info).show();
    }

    protected void onClientDisconnected(ClientInfo info) {
        Croutons.clientDisconnected(this, info).show();
    }

    public void showNowPlayingBar(boolean show){
        FragmentManager fragmentManager = getSupportFragmentManager();
        NowPlayingFragment nowPlaying = (NowPlayingFragment) fragmentManager.findFragmentById(R.id.fragment_now_playing);

        if (show && !nowPlaying.isVisible()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.now_playing_enter_anim, 0)
                    .show(nowPlaying)
                    .commitAllowingStateLoss();
        } else if (!show) {
            fragmentManager.beginTransaction()
                    .hide(nowPlaying)
                    .commitAllowingStateLoss();
        }
    }

}
