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
//import com.lwm.app.model.MusicPlayer;

public class ArtistsListFragment extends ListFragment {

//    MusicPlayer mp = MusicPlayer.getInstance();

    public ArtistsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArtistsCursorGetter cursorGetter = new ArtistsCursorGetter(getActivity());
//        Cursor cursor = cursorGetter.getArtists();

//        assert cursor != null;
        ListAdapter adapter = new ArtistsCursorAdapter(getActivity(), cursorGetter);

        setListAdapter(adapter);

        return inflater.inflate(R.layout.fragment_list_artists, container, false);
    }

//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//
////        ImageView imageView = (ImageView) v.findViewById(R.id.now_playing_indicator);
//        if(!mp.isPlaying() || mp.getCurrentSong() != position){
//
//            mp.setCurrentSong(position);
//            mp.playCurrentSong();
//
////            imageView.setVisibility(View.VISIBLE);
//
//            View nowPlaying = getActivity().findViewById(R.id.now_playing_fragment);
//            nowPlaying.setVisibility(View.VISIBLE);
//
//            TextView artist = (TextView) getActivity().findViewById(R.id.now_playing_artist);
//            artist.setText(mp.getCurrentPlaylist().getString(1));
//
//            TextView song = (TextView) getActivity().findViewById(R.id.now_playing_song);
//            song.setText(mp.getCurrentPlaylist().getString(2));
//
//        }
//
//    }

}
