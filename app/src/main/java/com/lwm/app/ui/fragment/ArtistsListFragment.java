package com.lwm.app.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lwm.app.R;
import com.lwm.app.adapter.ArtistsAdapter;
import com.lwm.app.helper.ArtistsCursorGetter;
import com.lwm.app.model.ArtistsList;
import com.lwm.app.ui.activity.ArtistInfoActivity;
//import com.lwm.app.player.LocalPlayer;

public class ArtistsListFragment extends ListFragment {

    private ArtistsList artistsList;

    public ArtistsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArtistsCursorGetter cursorGetter = new ArtistsCursorGetter(getActivity());
        Cursor artists = cursorGetter.getArtistsCursor();
        artistsList = new ArtistsList(artists);
//        artists.close();

        ListAdapter adapter = new ArtistsAdapter(getActivity(), artistsList);

        setListAdapter(adapter);

        return inflater.inflate(R.layout.fragment_list_artists, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), ArtistInfoActivity.class);
        intent.putExtra("artist_id", artistsList.getArtists().get(position).getId());
        startActivity(intent);
    }
}