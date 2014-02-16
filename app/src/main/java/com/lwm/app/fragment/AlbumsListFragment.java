/*
package com.lwm.app.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.lwm.app.R;
import com.lwm.app.adapter.AlbumsCursorAdapter;
import com.lwm.app.helper.AlbumsCursorGetter;

public class AlbumsListFragment extends ListFragment {

    public AlbumsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter(getActivity());
        Cursor cursor = cursorGetter.getAlbums();

        assert cursor != null;
        ListAdapter adapter = new AlbumsCursorAdapter(getActivity(), cursor);

        setListAdapter(adapter);

        return inflater.inflate(R.layout.fragment_list_albums, container, false);
    }
}
*/
