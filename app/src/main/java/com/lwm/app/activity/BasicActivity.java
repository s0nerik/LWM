package com.lwm.app.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lwm.app.R;
import com.lwm.app.fragment.ArtistsListFragment;
import com.lwm.app.fragment.SongsListFragment;
import com.lwm.app.lib.Connectivity;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.service.MusicService;

public class BasicActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    protected FragmentManager fragmentManager = getSupportFragmentManager();
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;

    protected void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                actionBar.getThemedContext(),
                R.array.spinner_names,
                android.R.layout.simple_list_item_1);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    protected void initNavigationDrawer(){

        String[] items = getResources().getStringArray(R.array.drawer_items);
//        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch(i){
                    case 0:
                        break;
                    case 1:

                        new Thread(new Runnable(){

                            @Override
                            public void run() {
                                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    if(wifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED){
                                        WifiInfo info = wifiManager.getConnectionInfo();
                                        if (info == null || !WifiAP.AP_NAME.equals(info.getSSID())) {
                                            // Device is connected to different AP or not connected at all
                                            Connectivity.connectToOpenAP(BasicActivity.this, WifiAP.AP_NAME);
                                        }
                                    }else{
                                        // Wifi is disabled, so let's turn it on and connect
                                        wifiManager.setWifiEnabled(true);

                                        // Wait until it Wifi is enabled
                                        try {
                                            while(!wifiManager.isWifiEnabled()){
                                                Thread.sleep(500);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Connectivity.connectToOpenAP(BasicActivity.this, WifiAP.AP_NAME);

                                        // Wait until Wifi is really connected
                                        try {
                                            while(!Connectivity.isConnectedWifi(BasicActivity.this)){
                                                Thread.sleep(500);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                startStreamPlayback();

                            }
                        }).start();

//                        Intent intent = new Intent(BroadcastActivity.this, ListenActivity.class);
//                        startActivity(intent);
                        break;
                }

            }

        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {

        switch(i){
            case 0: // All songs
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SongsListFragment())
                        .commit();
                return true;
            case 1: // Artists
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new ArtistsListFragment())
                        .commit();
                return true;
            case 2: // Albums
//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, new AlbumsListFragment())
//                        .commit();
                return true;

            default:
                return true;
        }

    }

    protected void startStreamPlayback(){
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY_STREAM);
        startService(intent);
    }

}
