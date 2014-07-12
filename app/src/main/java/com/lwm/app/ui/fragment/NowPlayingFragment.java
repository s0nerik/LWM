package com.lwm.app.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.activity.LocalPlaybackActivity;
import com.squareup.picasso.Picasso;

public class NowPlayingFragment extends Fragment
//        implements PlayerListener
{

    private ImageView albumArt;
    private ImageView playPauseButton;
    private TextView artist;
    private TextView title;

    View.OnClickListener onPlayPauseClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LocalPlayer player = App.getLocalPlayer();
            player.togglePause();
            setPlayButton(player.isPlaying());
        }
    };

    View.OnClickListener onNowPlayingBarClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), LocalPlaybackActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_now_playing, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        albumArt = (ImageView) view.findViewById(R.id.now_playing_cover);
        artist = (TextView) view.findViewById(R.id.now_playing_bar_artist);
        title = (TextView) view.findViewById(R.id.now_playing_bar_title);
        playPauseButton = (ImageView) view.findViewById(R.id.now_playing_bar_play_pause_button);
        playPauseButton.setOnClickListener(onPlayPauseClicked);
        view.findViewById(R.id.now_playing_bar_layout).setOnClickListener(onNowPlayingBarClicked);
        title.setSelected(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(App.localPlayerActive()){
            LocalPlayer player = App.getLocalPlayer();
//            player.registerListener(this);
            if(player.hasCurrentSong()){
                setCurrentSongInfo();
                setPlayButton(App.getLocalPlayer().isPlaying());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if(App.localPlayerActive()){
//            App.getLocalPlayer().unregisterListener(this);
//        }
    }

    public void setCurrentSongInfo(){
        if(App.localPlayerActive()) {
            LocalPlayer player = App.getLocalPlayer();
            if (player.hasCurrentSong()) {
                Song song = player.getCurrentSong();
                setAlbumArtFromUri(song.getAlbumArtUri());
                artist.setText(song.getArtist());
                title.setText(song.getTitle());
                setPlayButton(true);
            }
        }
    }

    public void setAlbumArtFromUri(Uri uri){
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(300, 300)
                .placeholder(R.drawable.no_cover)
                .centerCrop()
                .into(albumArt);
    }

    public void setPlayButton(boolean playing){
        playPauseButton.setImageResource(playing? R.drawable.ic_pause : R.drawable.ic_play);
    }

//    @Override
//    public void onSongChanged(Song song) {
//        setCurrentSongInfo();
//    }
//
//    @Override
//    public void onPlaybackPaused() {
//        setPlayButton(false);
//    }
//
//    @Override
//    public void onPlaybackStarted() {
//        setPlayButton(true);
//    }
}
