package app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.arasthel.swissknife.SwissKnife;
import com.arasthel.swissknife.annotations.Extra;
import com.arasthel.swissknife.annotations.InjectView;
import com.lwm.app.R;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import app.Utils;
import app.model.Artist;
import groovy.transform.CompileStatic;

@CompileStatic
public class ArtistInfoActivity extends BaseLocalActivity {

    @Inject
    Bus bus;

    @Inject
    Utils utils;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @Extra
    Artist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);
        SwissKnife.inject(this)
        SwissKnife.loadExtras(this)

        mToolbar.setTitle(utils.getArtistName(artist.getName()));
        mToolbar.setSubtitle("Albums: " + artist.getNumberOfAlbums());
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment albumsListFragment = new AlbumsListFragmentBuilder().artist(artist).build();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, albumsListFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

//    @Subscribe
//    public void playbackStarted(PlaybackStartedEvent event) {
//        showNowPlayingBar(true);
//    }

}