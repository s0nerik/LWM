package app.ui.activity

import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ListView
import app.PrefManager
import app.R
import app.adapter.NavigationDrawerListAdapter
import app.events.player.service.CurrentSongAvailableEvent
import app.ui.base.DaggerActivity
import app.ui.fragment.LocalMusicFragment
import app.ui.fragment.StationsAroundFragment
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.arasthel.swissknife.annotations.OnItemClick
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

import static android.media.AudioManager.*
import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import static android.view.KeyEvent.KEYCODE_VOLUME_UP

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class MainActivity extends DaggerActivity {

    @Inject
    Bus bus
    @Inject
    AudioManager audio
    @Inject
    PrefManager prefManager

    @InjectView(R.id.drawer_list)
    ListView mDrawerList
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout
    @InjectView(R.id.nowPlayingFrame)
    View mNowPlayingFrame

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        contentView = R.layout.activity_main
        SwissKnife.inject this
        bus.register this
        initNavigationDrawer()

//        startActivity(new Intent(this, SplashActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        showFragmentFromDrawer(prefManager.drawerSelection().getOr(0))
    }

    private void showFragmentFromDrawer(int i) {
        Fragment fragment
        switch (i) {
            case 0:
                fragment = new LocalMusicFragment()
                break
            case 1:
                fragment = new StationsAroundFragment()
                break
            default:
                fragment = new LocalMusicFragment()
                break
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    protected void initNavigationDrawer() {
        // Set the adapter for the list view
        mDrawerList.setAdapter(new NavigationDrawerListAdapter(this,
                getResources().getStringArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)))

        int activeFragment = prefManager.drawerSelection().getOr(0)

        mDrawerList.setItemChecked(activeFragment, true)
    }

    @Subscribe
    public void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
        mNowPlayingFrame.setVisibility(View.VISIBLE);
    }

    @OnItemClick(R.id.drawer_list)
    public void onDrawerItemClicked(int i) {
        prefManager.drawerSelection().put(i).apply();
        showFragmentFromDrawer(i);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Subscribe
    public void onToolbarAvailable(Toolbar toolbar) {
        def drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        mDrawerLayout.drawerListener = drawerToggle
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT)
        drawerToggle.syncState()
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(STREAM_MUSIC, ADJUST_RAISE, FLAG_SHOW_UI)
                return true
            case KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(STREAM_MUSIC, ADJUST_LOWER, FLAG_SHOW_UI)
                return true
            default:
                return super.onKeyDown(keyCode, event)
        }
    }

}
