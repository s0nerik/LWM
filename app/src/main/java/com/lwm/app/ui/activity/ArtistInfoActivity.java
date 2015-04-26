package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Artist;
import com.lwm.app.ui.fragment.AlbumsListFragmentBuilder;
import com.squareup.otto.Bus;
import com.tale.prettybundle.Extra;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
        ButterKnife.inject(this);

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
