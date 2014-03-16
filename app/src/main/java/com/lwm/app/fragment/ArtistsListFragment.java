package com.lwm.app.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.lwm.app.R;
import com.lwm.app.adapter.ArtistsCursorAdapter;
import com.lwm.app.helper.ArtistsCursorGetter;
//import com.lwm.app.player.LocalPlayer;

public class ArtistsListFragment extends ListFragment {

    public ArtistsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArtistsCursorGetter cursorGetter = new ArtistsCursorGetter(getActivity());

        ListAdapter adapter = new ArtistsCursorAdapter(getActivity(), cursorGetter);

        setListAdapter(adapter);

        return inflater.inflate(R.layout.fragment_list_artists, container, false);
    }

}