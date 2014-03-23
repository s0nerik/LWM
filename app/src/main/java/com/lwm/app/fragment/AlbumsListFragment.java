package com.lwm.app.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lwm.app.R;
import com.lwm.app.adapter.AlbumsCursorAdapter;
import com.lwm.app.helper.AlbumsCursorGetter;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Playlist;

public class AlbumsListFragment extends ListFragment {

    private Cursor albums;

    public AlbumsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter(getActivity());
        albums = cursorGetter.getAlbums();
        ListAdapter adapter = new AlbumsCursorAdapter(getActivity(), albums);
        setListAdapter(adapter);
        return inflater.inflate(R.layout.fragment_list_albums, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        albums.moveToPosition(position);
        long albumId = albums.getLong(AlbumsCursorGetter._ID);
        albums.close();
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack("albums_fragment")
                .replace(R.id.container,
                        new PlaylistFragment(
                            new Playlist(new SongsCursorGetter(getActivity())
                                    .getSongs(albumId))))
                .commit();
    }
}
