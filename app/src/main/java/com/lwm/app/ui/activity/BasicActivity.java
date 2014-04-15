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

//    public static final String DRAWER_SELECTION = "drawer_selection";
//
//    protected enum DrawerItems { SONGS, ARTISTS, ALBUMS, QUEUE }

//    protected FragmentManager fragmentManager = getSupportFragmentManager();
//    protected DrawerLayout drawerLayout;
//    protected ActionBarDrawerToggle drawerToggle;
//    protected SharedPreferences sharedPreferences;
//    protected ActionBar actionBar;
//    protected ListView drawerList;
//    protected int activeFragment;



//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        sharedPreferences = getPreferences(MODE_PRIVATE);
//    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        // Sync the toggle state after onRestoreInstanceState has occurred.
//        drawerToggle.syncState();
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

    @Override
    protected void onResume() {
        super.onResume();
        if(LocalPlayer.hasCurrentSong()) {
            App.getMusicService().getLocalPlayer().registerListener(this);
            showNowPlayingBar(true);
        }else{
            showNowPlayingBar(false);
        }
    }

    @Override
    protected void onPause() {
        super.onStop();
        if(LocalPlayer.hasCurrentSong()) {
            App.getMusicService().getLocalPlayer().unregisterListener();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(LocalPlayer.hasCurrentSong()) {
//            App.getMusicService().getLocalPlayer().unregisterListener();
//        }
//    }

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
//            if (LocalPlayer.hasCurrentSong()) {
                fragmentManager.beginTransaction()
                        .show(nowPlaying)
                        .commitAllowingStateLoss();

                nowPlaying.setCurrentSongInfo();

                LocalPlayer player = App.getMusicService().getLocalPlayer();
                nowPlaying.setPlayButton(player.isPlaying());
//            }
        } else {
            fragmentManager.beginTransaction()
                    .hide(nowPlaying)
                    .commitAllowingStateLoss();
        }
    }

}
