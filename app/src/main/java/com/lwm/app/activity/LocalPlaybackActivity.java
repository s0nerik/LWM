package com.lwm.app.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiAPListener;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.PlayerListener;

public class LocalPlaybackActivity extends PlaybackActivity implements
        WifiAPListener, PlayerListener {

    private LocalPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(App.isMusicServiceBound()){
            player = App.getMusicService().getLocalPlayer();
            playbackFragment.setPlayButton(player.isPlaying());
            playbackFragment.setShuffleButton(LocalPlayer.isShuffle());
        }
    }

    @Override
    public void onControlButtonClicked(View v){
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
                LocalPlayer.setShuffle(!LocalPlayer.isShuffle());
                playbackFragment.setShuffleButton(LocalPlayer.isShuffle());

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean("shuffle", LocalPlayer.isShuffle());
                editor.commit();

                break;

            case R.id.fragment_playback_repeat_button:
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
        player.unregisterListener();
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
    public void onEnableAP() {
        setMenuProgressIndicator(true);
    }

    @Override
    public void onAPEnabled() {
        setMenuProgressIndicator(false);
    }

    @Override
    public void onSongChanged(Song song) {
        setSongInfo(song);
    }
}