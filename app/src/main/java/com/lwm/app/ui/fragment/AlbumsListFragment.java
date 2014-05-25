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
import com.lwm.app.adapter.AlbumsAdapter;
import com.lwm.app.helper.AlbumsCursorGetter;
import com.lwm.app.model.AlbumsList;
import com.lwm.app.model.Artist;
import com.lwm.app.ui.activity.AlbumInfoActivity;

public class AlbumsListFragment extends ListFragment {

    private AlbumsList albumsList;
    private Artist artist;

    public AlbumsListFragment() {}

    public AlbumsListFragment(Artist artist) {
        this.artist = artist;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter(getActivity());
        Cursor albums;

        if(artist == null){
            albums = cursorGetter.getAlbumsCursor();
        }else{
            albums = cursorGetter.getAlbumsCursorByArtist(artist);
        }

        albumsList = new AlbumsList(albums);
//        albums.close();
        ListAdapter adapter = new AlbumsAdapter(getActivity(), albumsList);

        setListAdapter(adapter);
        return inflater.inflate(R.layout.fragment_list_albums, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(getActivity(), AlbumInfoActivity.class);
        intent.putExtra("album_id", albumsList.getAlbums().get(position).getId());
        startActivity(intent);
    }
}
