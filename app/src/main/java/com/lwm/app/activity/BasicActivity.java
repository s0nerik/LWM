package com.lwm.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.fragment.AlbumsListFragment;
import com.lwm.app.fragment.ArtistsListFragment;
import com.lwm.app.fragment.PlayersAroundFragment;
import com.lwm.app.fragment.SongsListFragment;
import com.lwm.app.service.MusicService;

public class BasicActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    protected FragmentManager fragmentManager = getSupportFragmentManager();
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;
    protected SharedPreferences state;
    protected ActionBar actionBar;
    protected ListView drawerList;
    protected int activePlaylist;

    protected void initActionBar(){
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                actionBar.getThemedContext(),
                R.array.spinner_names,
                android.R.layout.simple_list_item_1);
        adapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown);
        actionBar.setListNavigationCallbacks(adapter, this);
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
                showSelectedPlaylist(i);
                state.edit().putInt("current_filter", i).commit();
            }
        });

        activePlaylist = state.getInt("current_filter", 0);
        drawerList.setItemChecked(activePlaylist, true);
        showSelectedPlaylist(activePlaylist);

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
        state = getPreferences(MODE_PRIVATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    protected void showSelectedPlaylist(int i){
        switch(i){
            case 0: // All songs
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SongsListFragment())
                        .commit();
                break;
            case 1: // Artists
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new ArtistsListFragment())
                        .commit();
                break;
            case 2: // Albums
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new AlbumsListFragment())
                        .commit();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {

        switch(i){
            case 0:
                return true;

            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new PlayersAroundFragment(), "players_around_list")
                        .commit();

//                        Intent intent = new Intent(BroadcastActivity.this, ListenActivity.class);
//                        startActivity(intent);
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
