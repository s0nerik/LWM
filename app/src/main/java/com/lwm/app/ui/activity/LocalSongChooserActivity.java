package com.lwm.app.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.events.access_point.AccessPointStateEvent;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiApManager;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.Croutons;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.lwm.app.ui.fragment.ArtistsListFragment;
import com.lwm.app.ui.fragment.QueueFragment;
import com.lwm.app.ui.fragment.SongsListFragment;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LocalSongChooserActivity extends BaseLocalActivity {

    public static final String DRAWER_SELECTION = "drawer_selection";

    @Inject
    Resources resources;

    @Inject
    SharedPreferences sharedPreferences;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.drawer)
    ListView mDrawer;
    @InjectView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;

    private enum DrawerItem {SONGS, ARTISTS, ALBUMS, QUEUE}

    private MenuItem broadcastButton;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ActionBarDrawerToggle drawerToggle;

    private DrawerItem activeFragment;

    private Intent playerServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playerServiceIntent = new Intent(this, LocalPlayerService.class);
        startService(playerServiceIntent);

        setContentView(R.layout.activity_local_song_chooser);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
        initNavigationDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!player.isPlaying()) {
            Log.d(App.TAG, "stopService");
            stopService(playerServiceIntent);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();

        if (savedInstanceState == null) {
            showSelectedFragment(activeFragment);
        }

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
        mToolbar.setTitle(title);
    }

    protected void initNavigationDrawer() {
        // Set the adapter for the list view
        mDrawer.setAdapter(new NavigationDrawerListAdapter(this,
                getResources().getStringArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)));

        // Set the list's click listener
        mDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showSelectedFragment(DrawerItem.values()[i]);
                sharedPreferences.edit().putInt(DRAWER_SELECTION, i).apply();
                mDrawerLayout.closeDrawer(mDrawer);
            }
        });

        activeFragment = DrawerItem.values()[sharedPreferences.getInt(DRAWER_SELECTION, 0)];

        if (activeFragment == DrawerItem.QUEUE) {
            activeFragment = DrawerItem.SONGS;
        }

        mDrawer.setItemChecked(activeFragment.ordinal(), true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        mDrawerLayout.setDrawerListener(drawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
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
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.broadcast, menu);

        broadcastButton = menu.findItem(R.id.action_broadcast);

        WifiApManager manager = new WifiApManager(this);
        if (manager.isWifiApEnabled()) {
            setBroadcastButtonState(true);
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
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.action_broadcast:
                new WifiAP().toggleWiFiAP();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onAccessPointState(AccessPointStateEvent event) {
        switch (event.getState()) {
            case CHANGING:
                setMenuProgressIndicator(true);
                break;
            case DISABLED:
                setMenuProgressIndicator(false);
                setBroadcastButtonState(true);
                break;
            case ENABLED:
                setMenuProgressIndicator(false);
                setBroadcastButtonState(false);
                break;
        }
    }

    @Subscribe
    public void onClientConnected(ClientConnectedEvent event) {
        onClientConnected(event.getClientInfo());
    }

    @Subscribe
    public void onClientDisconnected(ClientDisconnectedEvent event) {
        onClientDisconnected(event.getClientInfo());
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Croutons.messageReceived(this, event.getMessage());
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
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}