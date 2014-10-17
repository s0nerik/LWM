package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.events.access_point.StopServerEvent;
import com.lwm.app.events.access_point.StartServerEvent;
import com.lwm.app.events.access_point.AccessPointStateChangingEvent;
import com.lwm.app.events.player.StartForegroundLocalPlayerEvent;
import com.lwm.app.events.player.binding.BindLocalPlayerServiceEvent;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.events.player.binding.LocalPlayerServiceBoundEvent;
import com.lwm.app.events.player.binding.UnbindLocalPlayerServiceEvent;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiApManager;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.lwm.app.ui.fragment.ArtistsListFragment;
import com.lwm.app.ui.fragment.QueueFragment;
import com.lwm.app.ui.fragment.SongsListFragment;
import com.squareup.otto.Subscribe;

public class LocalSongChooserActivity extends BasicActivity {

    public static final String DRAWER_SELECTION = "drawer_selection";

    private enum DrawerItem {SONGS, ARTISTS, ALBUMS, QUEUE}

    private MenuItem broadcastButton;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToggle;

    private DrawerItem activeFragment;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_local_song_chooser);

        App.getBus().post(new BindLocalPlayerServiceEvent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        initActionBar();
        initNavigationDrawer();

        if (savedInstanceState == null) {
            showSelectedFragment(activeFragment);
        }

        drawerToggle.syncState();
    }

    protected void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    protected Fragment getFragmentFromDrawer(DrawerItem i) {
        switch (i) {
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
        switch (activeFragment) {
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
                showSelectedFragment(DrawerItem.values()[i]);
                sharedPreferences.edit().putInt(DRAWER_SELECTION, i).apply();
                drawerLayout.closeDrawer(drawerList);
            }
        });

        activeFragment = DrawerItem.values()[sharedPreferences.getInt(DRAWER_SELECTION, 0)];

        if (activeFragment == DrawerItem.QUEUE) {
            activeFragment = DrawerItem.SONGS;
        }

        drawerList.setItemChecked(activeFragment.ordinal(), true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
    }

    protected void showSelectedFragment(DrawerItem i) {
        fragmentManager.beginTransaction()
                .replace(R.id.container, getFragmentFromDrawer(i))
                .commit();
        activeFragment = i;
        updateActionBarTitle();
    }

    @Subscribe
    public void playbackStarted(PlaybackStartedEvent event) {
        showNowPlayingBar(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActionBarTitle();
        App.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getBus().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.broadcast, menu);

        broadcastButton = menu.findItem(R.id.action_broadcast);

        WifiApManager manager = new WifiApManager(this);
        if (manager.isWifiApEnabled()) {
            setBroadcastButtonState(true);
            App.getBus().post(new StartServerEvent());
        } else {
            setBroadcastButtonState(false);
        }

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

    @Subscribe
    public void accessPointStateChanging(AccessPointStateChangingEvent event) {
        setMenuProgressIndicator(true);
    }

    @Subscribe
    public void accessPointEnabled(StartServerEvent event) {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(true);
    }

    @Subscribe
    public void accessPointDisabled(StopServerEvent event) {
        setMenuProgressIndicator(false);
        setBroadcastButtonState(false);
    }

    @Subscribe
    public void onLocalPlayerServiceBound(LocalPlayerServiceBoundEvent event) {
        player = event.getLocalPlayerService();
    }

    @Subscribe
    public void onClientConnected(ClientConnectedEvent event) {
        onClientConnected(event.getName());
    }

    @Subscribe
    public void onClientDisconnected(ClientDisconnectedEvent event) {
        onClientDisconnected(event.getName());
    }

    private void setBroadcastButtonState(boolean broadcasting) {
        if (broadcasting) {
            broadcastButton.setIcon(R.drawable.ic_action_broadcast_active);
        } else {
            broadcastButton.setIcon(R.drawable.ic_action_broadcast);
        }
    }

    @Override
    public void onBackPressed() {
        if (!player.isPlaying()) {
            App.getBus().post(new UnbindLocalPlayerServiceEvent());
        } else {
            App.getBus().post(new StartForegroundLocalPlayerEvent());
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}