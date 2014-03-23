package com.lwm.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.ui.fragment.NowPlayingFragment;
import com.lwm.app.ui.fragment.OnSongSelectedListener;
import com.lwm.app.ui.fragment.PlayersAroundFragment;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiAPListener;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.PlayerListener;
import com.lwm.app.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class BroadcastActivity extends BasicActivity implements
        OnSongSelectedListener, WifiAPListener, PlayerListener {

    private MusicService musicService;
    private NowPlayingFragment nowPlaying;
    private MenuItem broadcastButton;

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            switch(i.getAction()){
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    Log.d(App.TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
                    PlayersAroundFragment fragment = (PlayersAroundFragment) fragmentManager.findFragmentByTag("players_around_list");
                    if(fragment != null){
                        List<String> ssids = new ArrayList<>();
                        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                        for(ScanResult result:wm.getScanResults()){
                            if(result.SSID.endsWith(WifiAP.AP_NAME_POSTFIX)){
                                ssids.add(result.SSID.replace(WifiAP.AP_NAME_POSTFIX, ""));
                            }
                        }
                        fragment.setSSIDs(ssids);
                    }
                    break;
                case App.SERVICE_BOUND:
                    onServiceBound();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);

        initActionBar();
        initNavigationDrawer();

        nowPlaying = (NowPlayingFragment) fragmentManager.findFragmentById(R.id.fragment_now_playing);

        assert nowPlaying != null : "nowPlaying == null";
        fragmentManager.beginTransaction()
                .hide(nowPlaying)
                .commit();

    }

    private void onServiceBound(){
        musicService = App.getMusicService();

        assert musicService != null : "musicService == null";
        LocalPlayer player = musicService.getLocalPlayer();

        if(player != null){
            player.registerListener(this);
            if(actionBar.getSelectedNavigationIndex() == 0){
                showNowPlayingBar();
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d(App.TAG, "BroadcastActivity: onResume");
        super.onResume();
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(onBroadcast, new IntentFilter(App.SERVICE_BOUND));

        nowPlaying = (NowPlayingFragment) fragmentManager.findFragmentById(R.id.fragment_now_playing);

        if(App.isMusicServiceBound()){
            musicService = App.getMusicService();
            LocalPlayer player = musicService.getLocalPlayer();
            if(player != null){
                player.registerListener(this);

                if(actionBar.getSelectedNavigationIndex() == 0 && player.isInstanceActive()){
                    showNowPlayingBar();
                }

            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
        App.getMusicService().getLocalPlayer().unregisterListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.broadcast, menu);

        broadcastButton = menu.findItem(R.id.action_broadcast);

        return true;
    }

    public void setMenuProgressIndicator(boolean show){

        if(broadcastButton == null) return;

        if(show)
            MenuItemCompat.setActionView(broadcastButton, R.layout.progress_actionbar_broadcast);
        else
            MenuItemCompat.setActionView(broadcastButton, null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.action_broadcast:
                WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiAP wifiAP = new WifiAP();
                wifiAP.toggleWiFiAP(wm, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNowPlayingBarClicked(View v){
        switch(v.getId()){
            case R.id.fragment_now_playing:
                Intent intent = new Intent(this, LocalPlaybackActivity.class);
                startActivity(intent);
                break;
            case R.id.now_playing_bar_play_pause_button:
                LocalPlayer player = musicService.getLocalPlayer();
                player.togglePause();
                nowPlaying.setPlayButton(player.isPlaying());
                break;
        }
    }

    private void showNowPlayingBar(){

        Log.d(App.TAG, "showNowPlayingBar()");

        assert musicService != null : "showNowPlayingBar(): musicService == null";
        assert musicService.getLocalPlayer() != null : "showNowPlayingBar(): musicService.getLocalPlayer() == null";
        musicService.getLocalPlayer().registerListener(this);

        Song song = App.getMusicService().getLocalPlayer().getCurrentSong();

        if(song != null){
            NowPlayingFragment nowPlaying = (NowPlayingFragment) fragmentManager.findFragmentById(R.id.fragment_now_playing);
            fragmentManager.beginTransaction()
                    .show(nowPlaying)
                    .commit();

            nowPlaying.setArtist(song.getArtist());
            nowPlaying.setTitle(song.getTitle());
            nowPlaying.setAlbumArtFromUri(song.getAlbumArtUri());

            LocalPlayer player = musicService.getLocalPlayer();
            nowPlaying.setPlayButton(player.isPlaying());
        }
    }

    @Override
    public void onSongSelected(int position) {
        showNowPlayingBar();
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
        Log.d(App.TAG, "BroadcastActivity: onSongChanged");

        FragmentManager fm = getSupportFragmentManager();
        ListFragment listFragment = (ListFragment) fm.findFragmentById(R.id.container);
        int pos = App.getMusicService().getLocalPlayer().getCurrentListPosition();
        listFragment.setSelection(pos);
        showNowPlayingBar();
    }
}