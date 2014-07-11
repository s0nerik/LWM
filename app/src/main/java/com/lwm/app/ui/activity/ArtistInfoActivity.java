package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.google.gson.Gson;
import com.lwm.app.R;
import com.lwm.app.helper.ArtistsCursorGetter;
import com.lwm.app.model.Artist;
import com.lwm.app.ui.fragment.AlbumsListFragment;

public class ArtistInfoActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_info);

        long artistId = getIntent().getLongExtra("artist_id", -1);
        assert artistId != -1 : "artistId == -1";
        Artist artist = new ArtistsCursorGetter(this).getArtistById(artistId);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(artist.getName());
        actionBar.setSubtitle("ALBUMS: "+artist.getNumberOfAlbums());

        Fragment albumsListFragment = new AlbumsListFragment();
        Bundle args = new Bundle();
        args.putString("artist", new Gson().toJson(artist));
        albumsListFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, albumsListFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }
}
