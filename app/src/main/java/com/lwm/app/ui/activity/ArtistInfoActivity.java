package com.lwm.app.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.lwm.app.R;
import com.lwm.app.helper.ArtistsCursorGetter;
import com.lwm.app.model.Artist;
import com.lwm.app.player.PlayerListener;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.lwm.app.ui.fragment.OnSongSelectedListener;

public class ArtistInfoActivity extends BasicActivity implements
        OnSongSelectedListener, PlayerListener {

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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AlbumsListFragment(artist))
                .commit();
    }

    @Override
    public void onSongSelected(int position) {
//        listView.setItemChecked(position, true);
//        listView.setSelection(position);
    }

}
