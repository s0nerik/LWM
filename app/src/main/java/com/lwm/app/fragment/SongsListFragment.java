package com.lwm.app.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsCursorAdapter;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.BasePlayer;
import com.lwm.app.model.LocalPlayer;
import com.lwm.app.service.MusicService;

public class SongsListFragment extends ListFragment {

    public SongsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SongsCursorGetter cursorGetter = new SongsCursorGetter(getActivity());
        Cursor cursor = cursorGetter.getSongs();

        assert cursor != null;

        ListAdapter adapter = new SongsCursorAdapter(getActivity(), cursorGetter);

        setListAdapter(adapter);

        return inflater.inflate(R.layout.fragment_list_songs, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalPlayer mp = MusicService.getCurrentLocalPlayer();
        if(mp != null){
            ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
            listView.setItemChecked(mp.getCurrentListPosition(), true);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Intent intent = new Intent(getActivity(), MusicService.class);

        intent.setAction(MusicService.ACTION_PLAY_SONG);
        intent.putExtra(BasePlayer.PLAYLIST_POSITION, position);

        Log.d(App.TAG, ".startService");

        getActivity().startService(intent);

    }

}