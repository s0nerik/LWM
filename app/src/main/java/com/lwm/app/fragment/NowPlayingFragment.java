package com.lwm.app.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lwm.app.R;
import com.lwm.app.model.MusicPlayer;
import com.lwm.app.service.MusicService;

public class NowPlayingFragment extends Fragment {

    private ImageView albumArt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_now_playing, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        albumArt = (ImageView) view.findViewById(R.id.now_playing_cover);
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicPlayer player = MusicService.getCurrentPlayer();
        if(player != null){
            setAlbumArtFromUri(MusicService.getCurrentPlayer().getCurrentAlbumArtUri());
        }
    }

    public void setAlbumArtFromUri(Uri uri){
        albumArt.setImageURI(uri);
    }

}
