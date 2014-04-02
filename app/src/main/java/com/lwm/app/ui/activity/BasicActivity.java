package com.lwm.app.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lwm.app.R;
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.lwm.app.ui.fragment.ArtistsListFragment;
import com.lwm.app.ui.fragment.PlaylistFragment;
import com.lwm.app.ui.fragment.SongsListFragment;

public abstract class BasicActivity extends ActionBarActivity {

    public static final String DRAWER_SELECTION = "drawer_selection";

    protected enum DrawerItems { SONGS, ARTISTS, ALBUMS, PLAYLISTS }

    protected FragmentManager fragmentManager = getSupportFragmentManager();
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;
    protected SharedPreferences sharedPreferences;
    protected ActionBar actionBar;
    protected ListView drawerList;
    protected int activeFragment;

    protected void initActionBar(){
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    protected void initNavigationDrawer(){

        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new NavigationDrawerListAdapter(this,
                getResources().getStringArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)));

        // Set the list's click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showSelectedFragment(i);
                sharedPreferences.edit().putInt(DRAWER_SELECTION, i).commit();
            }
        });

        activeFragment = sharedPreferences.getInt(DRAWER_SELECTION, 0);
        drawerList.setItemChecked(activeFragment, true);
        showSelectedFragment(activeFragment);

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getPreferences(MODE_PRIVATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    protected void showSelectedFragment(int i){
        fragmentManager.beginTransaction()
                .replace(R.id.container, getFragmentFromDrawer(i))
                .commit();
        activeFragment = i;
        updateActionBarTitle();
    }

    protected Fragment getFragmentFromDrawer(int i){
        switch(DrawerItems.values()[i]){
            case SONGS:
                return new SongsListFragment();
            case ARTISTS:
                return new ArtistsListFragment();
            case ALBUMS:
                return new AlbumsListFragment();
            case PLAYLISTS:
                return new PlaylistFragment();
        }
        return null;
    }

    protected void updateActionBarTitle(){
        Resources resources = getResources();
        String title;
        Drawable icon;
        switch(DrawerItems.values()[activeFragment]){
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
            case PLAYLISTS:
                title = resources.getString(R.string.actionbar_title_playlists);
                icon = resources.getDrawable(R.drawable.ic_drawer_playlists_active);
                break;
            default:
                title = "Listen With Me!";
                icon = resources.getDrawable(R.drawable.ic_launcher);
                break;
        }
        actionBar.setTitle(title);
        actionBar.setIcon(icon);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

}
