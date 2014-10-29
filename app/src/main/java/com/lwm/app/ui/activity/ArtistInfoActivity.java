package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.helper.ArtistsCursorGetter;
import com.lwm.app.model.Artist;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class ArtistInfoActivity extends BaseLocalActivity {

    @Inject
    Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_info);

        long artistId = getIntent().getLongExtra("artist_id", -1);
        assert artistId != -1 : "artistId == -1";
        Artist artist = new ArtistsCursorGetter(this).getArtistById(artistId);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(new Utils(this).getArtistName(artist.getName()));
        actionBar.setSubtitle("ALBUMS: "+artist.getNumberOfAlbums());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(android.R.color.transparent);

        Fragment albumsListFragment = new AlbumsListFragment();
        Bundle args = new Bundle();
        args.putString("artist", new Gson().toJson(artist));
        albumsListFragment.setArguments(args);

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

    @Subscribe
    public void playbackStarted(PlaybackStartedEvent event) {
        showNowPlayingBar(true);
    }

}
