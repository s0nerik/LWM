package app.ui.activity

import android.media.AudioManager
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.Nullable
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.KeyEvent
import app.App
import app.R
import app.prefs.MainPrefs
import app.rx.RxBus
import app.ui.base.BaseActivity
import app.ui.fragment.LocalMusicFragment
import app.ui.fragment.StationsAroundFragment
import com.github.s0nerik.betterknife.annotations.InjectLayout
import groovy.transform.CompileStatic

import javax.inject.Inject

import static android.media.AudioManager.ADJUST_LOWER
import static android.media.AudioManager.ADJUST_RAISE
import static android.media.AudioManager.FLAG_SHOW_UI
import static android.media.AudioManager.STREAM_MUSIC
import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import static android.view.KeyEvent.KEYCODE_VOLUME_UP

@CompileStatic
@InjectLayout(value = R.layout.activity_main, injectAllViews = true)
public class MainActivity extends BaseActivity {

    int BUFFER_SEGMENT_SIZE = 1024
    int BUFFER_SEGMENT_COUNT = 512

    @Inject
    protected AudioManager audio

    @Inject
    protected MainPrefs mainPrefs

    DrawerLayout drawerLayout
    NavigationView navigation

    @Override
    void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        initNavigationDrawer()
    }

    @Override
    protected void initEventHandlersOnCreate() {
        super.initEventHandlersOnCreate()
        RxBus.on(Toolbar).compose(bindToLifecycle()).subscribe(this.&onEvent)
    }

    private void showFragmentFromDrawer(@IdRes int id) {
        Fragment fragment
        switch (id) {
            case R.id.local_music:
                fragment = new LocalMusicFragment()
                break
            case R.id.stations_around:
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
        navigation.navigationItemSelectedListener = {
            mainPrefs.putDrawerSelection it.itemId
            drawerLayout.closeDrawer Gravity.LEFT
            showFragmentFromDrawer it.itemId

            return true
        }

        showFragmentFromDrawer mainPrefs.drawerSelection
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

    // region Event handlers

    private void onEvent(Toolbar toolbar) {
        def drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.drawerListener = drawerToggle
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT)
        drawerToggle.syncState()
    }

    // endregion
}
