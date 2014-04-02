package com.lwm.app.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.ui.fragment.PlayersAroundFragment;

import java.util.ArrayList;
import java.util.List;

public class StationChooserActivity extends ActionBarActivity {

    FragmentManager fragmentManager;

    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            switch (i.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    Log.d(App.TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
                    PlayersAroundFragment fragment = (PlayersAroundFragment) fragmentManager.findFragmentById(R.id.fragment_players_around);
                    if (fragment != null) {
                        List<String> ssids = new ArrayList<>();
                        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                        for (ScanResult result : wm.getScanResults()) {
                            if (result.SSID.endsWith(WifiAP.AP_NAME_POSTFIX)) {
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
    protected void onResume() {
        super.onResume();
        fragmentManager = getSupportFragmentManager();
        registerReceiver(onBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_chooser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.station_chooser, menu);
        return true;
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
