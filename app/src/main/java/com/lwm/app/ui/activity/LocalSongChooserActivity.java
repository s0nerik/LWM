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
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.server.ClientConnectedEvent;
import com.lwm.app.events.server.ClientDisconnectedEvent;
import com.lwm.app.lib.WifiAP;
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
import butterknife.OnItemClick;

public class LocalSongChooserActivity extends BaseLocalActivity {

    public static final String DRAWER_SELECTION = "drawer_selection";

    @Inject
    Resources resources;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    WifiAP wifiAP;

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

    protected void initToolbar() {
        String title;
        int menuId;
        switch (activeFragment) {
            case SONGS:
                title = resources.getString(R.string.actionbar_title_songs);
                menuId = R.menu.local_broadcast_shuffle;
                break;
            case ALBUMS:
                title = resources.getString(R.string.actionbar_title_albums);
                menuId = R.menu.local_broadcast;
                break;
            case ARTISTS:
                title = resources.getString(R.string.actionbar_title_artists);
                menuId = R.menu.local_broadcast;
                break;
            case QUEUE:
                title = resources.getString(R.string.actionbar_title_queue);
                menuId = R.menu.local_broadcast_shuffle;
                break;
            default:
                title = "Listen With Me!";
                menuId = 0;
                break;
        }
        mToolbar.setTitle(title);

        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(menuId);
    }

    protected void initNavigationDrawer() {
        // Set the adapter for the list view
        mDrawer.setAdapter(new NavigationDrawerListAdapter(this,
                getResources().getStringArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)));

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
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initToolbar();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @OnItemClick(R.id.drawer)
    public void onDrawerItemClicked(int i) {
        showSelectedFragment(DrawerItem.values()[i]);
        mDrawerLayout.closeDrawer(mDrawer);
        sharedPreferences.edit().putInt(DRAWER_SELECTION, i).apply();
    }

    @Subscribe
    public void playbackStarted(PlaybackStartedEvent event) {
        showNowPlayingBar(true);
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