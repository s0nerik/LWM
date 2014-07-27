package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.event.access_point.AccessPointDisabledEvent;
import com.lwm.app.event.access_point.AccessPointEnabledEvent;
import com.lwm.app.event.access_point.AccessPointStateChangingEvent;
import com.lwm.app.event.player.PlaybackPausedEvent;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiApManager;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;
import com.squareup.otto.Subscribe;

public class LocalPlaybackActivity extends PlaybackActivity {

    private LocalPlayerService player;

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
            player = App.getLocalPlayerService();
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
                player.shuffleQueue();
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
        App.getEventBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getEventBus().unregister(this);
    }

    @Override
    protected void setSongInfo(Song song) {
        View v = actionBar.getCustomView();
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView subtitle = (TextView) v.findViewById(R.id.subtitle);

        title.setText(song.getTitle());
        subtitle.setText(song.getArtist());

        initSeekBarUpdater(player.getPlayer());

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
                onBackPressed();
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

        WifiApManager manager = new WifiApManager(this);
        setBroadcastButtonState(manager.isWifiApEnabled());

        return true;
    }

    @Subscribe
    public void accessPointStateChanging(AccessPointStateChangingEvent event) {
        setMenuProgressIndicator(true);
    }

    @Subscribe
    public void accessPointEnabled(AccessPointEnabledEvent event) {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(true);
    }

    @Subscribe
    public void accessPointDisabled(AccessPointDisabledEvent event) {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(false);
    }

    private void setBroadcastButtonState(boolean broadcasting) {
        if (broadcastButton != null) if (broadcasting) {
            broadcastButton.setIcon(R.drawable.ic_action_broadcast_active);
        } else {
            broadcastButton.setIcon(R.drawable.ic_action_broadcast);
        }
    }

    @Subscribe
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        playbackFragment.setPlayButton(false);
    }

    @Subscribe
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        setSongInfo(event.getSong());
        playbackFragment.setPlayButton(true);
    }

    @Override
    public void onBackPressed() {
//        if (fromNotification && App.localPlayerActive() && App.getLocalPlayerService().isPlaying()) {
//            new NowPlayingNotification(this).show();
//            finish();
//        } else {
            super.onBackPressed();
//        }
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}