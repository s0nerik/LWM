package app.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import app.Utils
import app.model.Artist
import app.ui.fragment.AlbumsListFragment
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.Extra
import com.arasthel.swissknife.annotations.InjectView
import app.R
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
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

        Fragment albumsListFragment = AlbumsListFragment.create(artist);

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
