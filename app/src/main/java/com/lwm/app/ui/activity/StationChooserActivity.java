package com.lwm.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.events.player.binding.BindStreamPlayerServiceEvent;
import com.lwm.app.events.wifi.WifiScanResultsAvailableEvent;
import com.lwm.app.events.wifi.WifiStateChangedEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiApManager;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class StationChooserActivity extends ActionBarActivity {

    private FragmentManager fragmentManager;

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            switch (i.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    Log.d(App.TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
                    App.getBus().post(new WifiScanResultsAvailableEvent(wm.getScanResults()));
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    Log.d(App.TAG, "WIFI_STATE_CHANGED_ACTION");
                    App.getBus().post(new WifiStateChangedEvent(wm));
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        fragmentManager = getSupportFragmentManager();
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        toggleNoWifiFrame((WifiManager) getSystemService(WIFI_SERVICE));
        App.getBus().register(this);
    }

    @Subscribe
    public void onWifiStateChanged(WifiStateChangedEvent event){
        toggleNoWifiFrame(event.getWifiManager());
    }

    private void toggleNoWifiFrame(WifiManager wm) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment playersAround = fragmentManager.findFragmentById(R.id.fragment_players_around);
        if (!wm.isWifiEnabled()) {
            if (playersAround.isVisible()) {
                Log.d(App.TAG, "Hide players around");
                findViewById(R.id.no_wifi_frame).setVisibility(View.VISIBLE);
                fragmentTransaction
                        .hide(playersAround)
                        .commit();
            }
        } else {
            Log.d(App.TAG, "Show players around");
            findViewById(R.id.no_wifi_frame).setVisibility(View.GONE);
            fragmentTransaction
                    .show(playersAround)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
        App.getBus().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_chooser);
        ButterKnife.inject(this);
        App.getBus().post(new BindStreamPlayerServiceEvent());
    }

    @OnClick(R.id.no_wifi_frame)
    public void onNoWifiClicked() {
        WifiAP wifiAP = new WifiAP();
        WifiApManager manager = new WifiApManager(this);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiApEnabled()) {
            wifiAP.toggleWiFiAP(wifiManager, this);
        } else {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.station_chooser, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
