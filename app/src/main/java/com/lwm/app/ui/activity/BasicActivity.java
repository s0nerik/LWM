package com.lwm.app.ui.activity;

import android.content.Context;
import android.media.AudioManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.PlayerListener;
import com.lwm.app.ui.fragment.NowPlayingFragment;

public class BasicActivity extends ActionBarActivity implements PlayerListener {

    private LocalPlayer player;

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

    @Override
    protected void onResume() {
        super.onResume();
        player = App.getMusicService().getLocalPlayer();
        if(player.hasCurrentSong()) {
            player.registerListener(this);
            showNowPlayingBar(true);
        }else{
            showNowPlayingBar(false);
        }
    }

    @Override
    protected void onPause() {
        super.onStop();
        if(player.hasCurrentSong()) {
            App.getMusicService().getLocalPlayer().unregisterListener();
        }
    }

    @Override
    public void onSongChanged(Song song) {
        showNowPlayingBar(true);
    }

    @Override
    public void onPlaybackPaused() {

    }

    @Override
    public void onPlaybackStarted() {

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

            LocalPlayer player = App.getMusicService().getLocalPlayer();
            nowPlaying.setPlayButton(player.isPlaying());
        } else {
            fragmentManager.beginTransaction()
                    .hide(nowPlaying)
                    .commitAllowingStateLoss();
        }
    }

}
