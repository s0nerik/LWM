package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiAPListener;
import com.lwm.app.lib.WifiApManager;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.lwm.app.ui.fragment.ArtistsListFragment;
import com.lwm.app.ui.fragment.OnSongSelectedListener;
import com.lwm.app.ui.fragment.QueueFragment;
import com.lwm.app.ui.fragment.SongsListFragment;
import com.lwm.app.ui.notification.NowPlayingNotification;

public class LocalSongChooserActivity extends BasicActivity implements
        WifiAPListener, OnSongSelectedListener {

    public static final String DRAWER_SELECTION = "drawer_selection";

    private enum DrawerItems {SONGS, ARTISTS, ALBUMS, QUEUE}

    private MenuItem broadcastButton;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToggle;
    private int activeFragment;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        if(activeFragment == 0){
            ((SongsListFragment) fragmentManager.findFragmentById(R.id.container)).onServiceBound();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_local_song_chooser);

        initActionBar();
        initNavigationDrawer();

        if (savedInstanceState == null) {
            showSelectedFragment(sharedPreferences.getInt(DRAWER_SELECTION, 0));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    protected void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    protected Fragment getFragmentFromDrawer(int i) {
        switch (DrawerItems.values()[i]) {
            case SONGS:
                return new SongsListFragment();
            case ARTISTS:
                return new ArtistsListFragment();
            case ALBUMS:
                return new AlbumsListFragment();
            case QUEUE:
                return new QueueFragment();
        }
        return null;
    }

    protected void updateActionBarTitle() {
        Resources resources = getResources();
        String title;
        Drawable icon;
        switch (DrawerItems.values()[activeFragment]) {
            case SONGS:
                title = resources.getString(R.string.actionbar_title_songs);
                icon = resources.getDrawable(R.drawable.ic_drawer_songs_active);
                break;
            case ALBUMS:
                title = resources.getString(R.string.actionbar_title_albums);
                icon = resources.getDrawable(R.drawable.ic_drawer_albums_active);
                break;
            case ARTISTS:
                title = resources.getString(R.string.actionbar_title_artists);
                icon = resources.getDrawable(R.drawable.ic_drawer_artists_active);
                break;
            case QUEUE:
                title = resources.getString(R.string.actionbar_title_queue);
                icon = resources.getDrawable(R.drawable.ic_drawer_queue_active);
                break;
            default:
                title = "Listen With Me!";
                icon = resources.getDrawable(R.drawable.ic_launcher);
                break;
        }
        actionBar.setTitle(title);
        actionBar.setIcon(icon);
    }

    protected void initNavigationDrawer() {
        final ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new NavigationDrawerListAdapter(this,
                getResources().getStringArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)));

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showSelectedFragment(i);
                sharedPreferences.edit().putInt(DRAWER_SELECTION, i).commit();
                drawerLayout.closeDrawer(drawerList);
            }
        });

        activeFragment = sharedPreferences.getInt(DRAWER_SELECTION, 0);
        drawerList.setItemChecked(activeFragment, true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.setDrawerListener(drawerToggle);
    }

    protected void showSelectedFragment(int i) {
        fragmentManager.beginTransaction()
                .replace(R.id.container, getFragmentFromDrawer(i))
                .commit();
        activeFragment = i;
        updateActionBarTitle();
    }

    @Override
    protected void onResume() {
        Log.d(App.TAG, "LocalSongChooserActivity: onResume");
        super.onResume();
        updateActionBarTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.broadcast, menu);

        broadcastButton = menu.findItem(R.id.action_broadcast);

        setBroadcastButtonState(0);

        return true;
    }

    public void setMenuProgressIndicator(boolean show) {

        if (broadcastButton == null) return;

        if (show)
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
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (id) {
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
    public void onSongSelected(int position) {
        showNowPlayingBar(true);
    }

    private void setBroadcastButtonState(int wait) {
        final WifiApManager manager = new WifiApManager(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (manager.isWifiApEnabled()) {
                    broadcastButton.setIcon(R.drawable.ic_action_broadcast_active);
                } else {
                    broadcastButton.setIcon(R.drawable.ic_action_broadcast);
                }
            }
        }, wait);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (App.localPlayerActive() && App.getLocalPlayer().isPlaying()) {
            new NowPlayingNotification(this).show();
        }
    }
}