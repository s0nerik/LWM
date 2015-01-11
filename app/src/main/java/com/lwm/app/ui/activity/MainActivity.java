package com.lwm.app.ui.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ListView;

import com.lwm.app.PrefManager;
import com.lwm.app.R;
import com.lwm.app.adapter.NavigationDrawerListAdapter;
import com.lwm.app.ui.base.DaggerActivity;
import com.lwm.app.ui.fragment.LocalMusicFragment;
import com.lwm.app.ui.fragment.StationsAroundFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public class MainActivity extends DaggerActivity {

    private ActionBarDrawerToggle drawerToggle;

    @Inject
    Bus bus;

    @Inject
    PrefManager prefManager;

    @InjectView(R.id.drawer_list)
    ListView mDrawerList;
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        bus.register(this);
        initNavigationDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        showFragmentFromDrawer(prefManager.drawerSelection().getOr(0));
    }

    private void showFragmentFromDrawer(int i) {
        Fragment fragment;
        switch (i) {
            case 0:
                fragment = new LocalMusicFragment();
                break;
            case 1:
                fragment = new StationsAroundFragment();
                break;
            default:
                fragment = new LocalMusicFragment();
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    protected void initNavigationDrawer() {
        // Set the adapter for the list view
        mDrawerList.setAdapter(new NavigationDrawerListAdapter(this,
                getResources().getStringArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)));

        int activeFragment = prefManager.drawerSelection().getOr(0);

        mDrawerList.setItemChecked(activeFragment, true);
    }

    @OnItemClick(R.id.drawer_list)
    public void onDrawerItemClicked(int i) {
        prefManager.drawerSelection().put(i).apply();
        showFragmentFromDrawer(i);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Subscribe
    public void onToolbarAvailable(Toolbar toolbar) {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        mDrawerLayout.setDrawerListener(drawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        drawerToggle.syncState();
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
