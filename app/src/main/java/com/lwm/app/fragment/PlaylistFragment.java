package com.lwm.app.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lwm.app.R;
import com.lwm.app.adapter.PlaylistAdapter;
import com.lwm.app.model.Playlist;
import com.lwm.app.player.LocalPlayer;

public class PlaylistFragment extends ListFragment {

    private Playlist playlist;
    private LocalPlayer player;

    public PlaylistFragment() {
        playlist = LocalPlayer.getPlaylist();
    }

    public PlaylistFragment(Playlist playlist){
        this.playlist = playlist;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setListAdapter(new PlaylistAdapter(getActivity(), playlist));
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
