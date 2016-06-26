package app.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import app.App
import app.R
import app.Utils
import app.models.Artist
import app.ui.fragment.AlbumsListFragment
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.Extra
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.activity_artist_info, injectAllViews = true)
public class ArtistInfoActivity extends BaseLocalActivity {

    @Inject
    protected Utils utils

    @InjectView(R.id.toolbar)
    Toolbar mToolbar

    @Extra
    Artist artist

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
        BetterKnife.loadExtras(this)

        mToolbar.setTitle utils.getArtistName(artist.name)
        mToolbar.setSubtitle "Albums: ${artist.numberOfAlbums}, Songs: ${artist.numberOfSongs}"
        mToolbar.backgroundColor = ColorGenerator.DEFAULT.getColor(artist.name)
        supportActionBar = mToolbar
        getSupportActionBar().displayHomeAsUpEnabled = true

        Fragment albumsListFragment = AlbumsListFragment.create artist

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, albumsListFragment)
                .commit()
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

//    @Subscribe
//    public void playbackStarted(PlaybackStartedEvent event) {
//        showNowPlayingBar(true);
//    }

}
