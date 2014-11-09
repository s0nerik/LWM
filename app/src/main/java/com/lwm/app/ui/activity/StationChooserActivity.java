package com.lwm.app.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.events.wifi.WifiScanResultsAvailableEvent;
import com.lwm.app.events.wifi.WifiStateChangedEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.ui.base.DaggerActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class StationChooserActivity extends DaggerActivity {

    private FragmentManager fragmentManager;

    @Inject
    Bus bus;

    @Inject
    WifiAP wifiAP;

    @Inject
    WifiManager wifiManager;

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            switch (i.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    Log.d(App.TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
                    bus.post(new WifiScanResultsAvailableEvent(wm.getScanResults()));
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    Log.d(App.TAG, "WIFI_STATE_CHANGED_ACTION");
                    bus.post(new WifiStateChangedEvent(wm));
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        fragmentManager = getFragmentManager();
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        toggleNoWifiFrame((WifiManager) getSystemService(WIFI_SERVICE));
        bus.register(this);
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
        bus.unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_chooser);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.no_wifi_frame)
    public void onNoWifiClicked() {
        if (wifiAP.isEnabled()) {
            wifiAP.toggleWiFiAP();
        } else {
            wifiManager.setWifiEnabled(true);
        }
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
