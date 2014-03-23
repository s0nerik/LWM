package com.lwm.app.fragment;

import android.app.Activity;
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
import com.lwm.app.adapter.PlaylistAdapter;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Playlist;
import com.lwm.app.player.LocalPlayer;

public class SongsListFragment extends ListFragment {

    OnSongSelectedListener mCallback;

    private Playlist playlist;
    private LocalPlayer player;
    private ListView listView;
    private int currentPosition = -1;

    private boolean isFirstClick = true;

    private final static int SMOOTH_SCROLL_MAX = 50;

    public SongsListFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSongSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SongsCursorGetter cursorGetter = new SongsCursorGetter(getActivity());

        playlist = new Playlist(cursorGetter.getSongs());

        ListAdapter adapter = new PlaylistAdapter(getActivity(), playlist);

        setListAdapter(adapter);

        return inflater.inflate(R.layout.fragment_list_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(App.isMusicServiceBound()){
            int pos = App.getMusicService().getLocalPlayer().getCurrentListPosition();
            listView.setItemChecked(pos, true);
            listView.setSelection(pos);
            currentPosition = pos;
        }
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        listView.setItemChecked(position, true);

        Log.d(App.TAG, "setSelection: "+Math.abs(position - currentPosition));

        if(Math.abs(position - currentPosition) <= SMOOTH_SCROLL_MAX){
            listView.smoothScrollToPosition(position);
        }else{
            listView.setSelection(position);
        }
        currentPosition = position;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(App.TAG, "SongsListFragment: onListItemClick");
        if(App.isMusicServiceBound()){

            if(isFirstClick){
                App.getMusicService().setLocalPlayer(new LocalPlayer(getActivity(), playlist));
                isFirstClick = false;
            }

            LocalPlayer player = App.getMusicService().getLocalPlayer();
            player.play(position);
        }
        mCallback.onSongSelected(position);
    }

}