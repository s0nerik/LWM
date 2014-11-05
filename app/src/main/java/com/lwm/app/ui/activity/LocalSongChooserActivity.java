package com.lwm.app.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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

    private View broadcastProgress;
    private ImageView broadcastIcon;

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

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ActionBarDrawerToggle drawerToggle;

    private DrawerItem activeFragment = DrawerItem.SONGS;

    private Intent playerServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playerServiceIntent = new Intent(this, LocalPlayerService.class);
        startService(playerServiceIntent);

        setContentView(R.layout.activity_local_song_chooser);
        ButterKnife.inject(this);

        initToolbar();
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

    protected void updateToolbarAdditionalInfo() {
        String title;
        switch (activeFragment) {
            case SONGS:
                title = resources.getString(R.string.actionbar_title_songs);
                mToolbar.inflateMenu(R.menu.item_shuffle);
                break;
            case ALBUMS:
                title = resources.getString(R.string.actionbar_title_albums);
                break;
            case ARTISTS:
                title = resources.getString(R.string.actionbar_title_artists);
                break;
            case QUEUE:
                title = resources.getString(R.string.actionbar_title_queue);
                mToolbar.inflateMenu(R.menu.item_shuffle);
                break;
            default:
                title = "Listen With Me!";
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
                mDrawerLayout.closeDrawer(mDrawer);
                sharedPreferences.edit().putInt(DRAWER_SELECTION, i).apply();
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

    private void initToolbar() {
        mToolbar.getMenu().clear();
        updateToolbarAdditionalInfo();
        mToolbar.inflateMenu(R.menu.item_broadcast);
        View broadcastButton = mToolbar.findViewById(R.id.action_broadcast);
        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WifiAP().toggleWiFiAP();
            }
        });

        broadcastProgress = broadcastButton.findViewById(R.id.progress);
        broadcastIcon = (ImageView) broadcastButton.findViewById(R.id.icon);

        if (new WifiApManager(this).getWifiApState() == WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED) {
            setBroadcastButtonEnabled(true);
        } else {
            setBroadcastButtonEnabled(false);
        }
    }

    private void showBroadcastProgressBar(boolean show) {
        broadcastProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        broadcastIcon.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setBroadcastButtonEnabled(boolean enabled) {
        broadcastIcon.setColorFilter(getResources().getColor(enabled? R.color.orange_main : android.R.color.white));
    }

    protected void showSelectedFragment(DrawerItem i) {
        fragmentManager.beginTransaction()
                .replace(R.id.container, getFragmentFromDrawer(i))
                .commit();
        activeFragment = i;
        initToolbar();
    }

    @Subscribe
    public void playbackStarted(PlaybackStartedEvent event) {
        showNowPlayingBar(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initToolbar();
        bus.register(this);

        WifiApManager manager = new WifiApManager(this);
        if (manager.isWifiApEnabled()) {
            setBroadcastButtonEnabled(true);
        } else {
            setBroadcastButtonEnabled(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onAccessPointStateChange(AccessPointStateEvent event) {
        switch (event.getState()) {
            case CHANGING:
                showBroadcastProgressBar(true);
                break;
            case DISABLED:
                showBroadcastProgressBar(false);
                setBroadcastButtonEnabled(false);
                break;
            case ENABLED:
                showBroadcastProgressBar(false);
                setBroadcastButtonEnabled(true);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}