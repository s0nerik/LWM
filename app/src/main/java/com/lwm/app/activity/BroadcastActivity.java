package com.lwm.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.fragment.NowPlayingFragment;
import com.lwm.app.fragment.PlayersAroundFragment;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.model.BasePlayer;
import com.lwm.app.model.LocalPlayer;
import com.lwm.app.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class BroadcastActivity extends BasicActivity {

    private NowPlayingFragment nowPlaying;
    private MenuItem broadcastButton;

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            switch(i.getAction()){
                case BasePlayer.PLAYBACK_STARTED:
                    Log.d(App.TAG, "Received LocalPlayer.PLAYBACK_STARTED");
                    showNowPlayingBar();
                    nowPlaying.setAlbumArtFromUri(MusicService.getCurrentLocalPlayer().getCurrentAlbumArtUri());
                    break;

                case BasePlayer.SONG_CHANGED:
                    // ListView changes
                    int playlistPosition = i.getIntExtra(BasePlayer.PLAYLIST_POSITION, -1);

                    assert playlistPosition != -1;
                    ListView listView = (ListView) findViewById(android.R.id.list);
                    listView.setItemChecked(playlistPosition, true);

                    // Now playing fragment changes
                    TextView title = (TextView) findViewById(R.id.now_playing_bar_song);
                    title.setText(MusicService.getCurrentLocalPlayer().getCurrentTitle());

                    TextView artist = (TextView) findViewById(R.id.now_playing_bar_artist);
                    artist.setText(MusicService.getCurrentLocalPlayer().getCurrentArtist());

                    nowPlaying.setAlbumArtFromUri(MusicService.getCurrentLocalPlayer().getCurrentAlbumArtUri());
                    break;

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

        assert nowPlaying != null;
        fragmentManager.beginTransaction()
                .hide(nowPlaying)
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(onBroadcast, new IntentFilter(BasePlayer.SONG_CHANGED));
        registerReceiver(onBroadcast, new IntentFilter(BasePlayer.PLAYBACK_STARTED));
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if(MusicService.getCurrentLocalPlayer() != null){
            showNowPlayingBar();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
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

    public void onNowPlayingButtonClicked(View v){
        Intent intent = new Intent(this, LocalPlaybackActivity.class);
        startActivity(intent);
    }

    private void showNowPlayingBar(){
        Fragment nowPlaying = fragmentManager.findFragmentById(R.id.fragment_now_playing);
        fragmentManager.beginTransaction()
                .show(nowPlaying)
                .commit();

        LocalPlayer player = MusicService.getCurrentLocalPlayer();
        TextView artist = (TextView) findViewById(R.id.now_playing_bar_artist);
        artist.setText(player.getCurrentArtist());

        TextView song = (TextView) findViewById(R.id.now_playing_bar_song);
        song.setText(player.getCurrentTitle());
    }

}