package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiAPListener;
import com.lwm.app.lib.WifiApManager;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.PlayerListener;
import com.lwm.app.ui.notification.NowPlayingNotification;

public class LocalPlaybackActivity extends PlaybackActivity implements
        WifiAPListener, PlayerListener {

    private LocalPlayer player;

    private boolean fromNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
        fromNotification = getIntent().getBooleanExtra("from_notification", false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(App.localPlayerActive()) {
            player = App.getLocalPlayer();
            playbackFragment.setPlayButton(player.isPlaying());
            playbackFragment.setShuffleButton(player.isShuffle());
            playbackFragment.setRepeatButton(player.isRepeat());
        }
    }

    @Override
    public void onControlButtonClicked(View v){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        switch(v.getId()){
            case R.id.fragment_playback_next:
                player.nextSong();
                break;

            case R.id.fragment_playback_prev:
                player.prevSong();
                break;

            case R.id.fragment_playback_play_pause:
                player.togglePause();
                playbackFragment.setPlayButton(player.isPlaying());
                break;

            case R.id.fragment_playback_shuffle_button:
                player.setShuffle(!player.isShuffle());
                playbackFragment.setShuffleButton(player.isShuffle());

                editor.putBoolean("shuffle", player.isShuffle());
                editor.commit();

                break;

            case R.id.fragment_playback_repeat_button:
                player.setRepeat(!player.isRepeat());
                playbackFragment.setRepeatButton(player.isRepeat());

                editor.putBoolean("repeat", player.isRepeat());
                editor.commit();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSongInfo(player.getCurrentSong());
        player.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.unregisterListener(this);
    }

    @Override
    protected void setSongInfo(Song song) {
        View v = actionBar.getCustomView();
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView subtitle = (TextView) v.findViewById(R.id.subtitle);

        title.setText(song.getTitle());
        subtitle.setText(song.getArtist());

        initSeekBarUpdater(player);

        // Now playing fragment changes
        playbackFragment.setDuration(song.getDurationString());
        playbackFragment.setPlayButton(player.isPlaying());

        // Change background if album art has changed
        long newAlbumId = song.getAlbumId();
        if(newAlbumId != currentAlbumId){
            playbackFragment.setAlbumArtFromUri(song.getAlbumArtUri());
            playbackFragment.setBackgroundImageUri(song.getAlbumArtUri());
            currentAlbumId = newAlbumId;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, LocalSongChooserActivity.class));
                return true;
            case R.id.action_broadcast:
                WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiAP wifiAP = new WifiAP();
                wifiAP.toggleWiFiAP(wm, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setMenuProgressIndicator(boolean show){

        if(broadcastButton == null) return;

        if(show)
            MenuItemCompat.setActionView(broadcastButton, R.layout.progress_actionbar_broadcast);
        else
            MenuItemCompat.setActionView(broadcastButton, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.local_playback, menu);

        broadcastButton = menu.findItem(R.id.action_broadcast);

        setBroadcastButtonState(0);
        return true;
    }

    @Override
    public void onChangeAPState() {
        setMenuProgressIndicator(true);
    }

    @Override
    public void onAPStateChanged() {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(4000);
    }

    @Override
    public void onSongChanged(Song song) {
        setSongInfo(song);
    }

    @Override
    public void onPlaybackPaused() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playbackFragment.setPlayButton(false);
            }
        });
    }

    @Override
    public void onPlaybackStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playbackFragment.setPlayButton(true);
            }
        });
    }

    private void setBroadcastButtonState(int wait){
        final WifiApManager manager = new WifiApManager(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(manager.isWifiApEnabled()){
                    broadcastButton.setIcon(R.drawable.ic_action_broadcast_active);
                }else{
                    broadcastButton.setIcon(R.drawable.ic_action_broadcast);
                }
            }
        }, wait);

    }

    @Override
    public void onBackPressed() {
        if (fromNotification && App.localPlayerActive() && App.getLocalPlayer().isPlaying()) {
            new NowPlayingNotification(this).show();
            finish();
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}