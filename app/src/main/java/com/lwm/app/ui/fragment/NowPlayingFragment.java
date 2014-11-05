package com.lwm.app.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.activity.LocalPlaybackActivity;
import com.lwm.app.ui.base.DaggerFragment;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class NowPlayingFragment extends DaggerFragment {

    private ImageView albumArt;
    private ImageView playPauseButton;
    private TextView artist;
    private TextView title;

    @Inject
    Utils utils;

    @Inject
    Bus bus;

    @Inject
    LocalPlayer player;

    View.OnClickListener onPlayPauseClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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

    public void setCurrentSongInfo(){
        if (player.hasCurrentSong()) {
            Song song = player.getCurrentSong();
            setAlbumArtFromUri(song.getAlbumArtUri());
            artist.setText(utils.getArtistName(song.getArtist()));
            title.setText(song.getTitle());
            setPlayButton(true);
        }
    }

    public void setAlbumArtFromUri(Uri uri){
        Utils.setAlbumArtFromUri(getActivity(), albumArt, uri);
    }

    public void setPlayButton(boolean playing){
        playPauseButton.setImageResource(playing? R.drawable.ic_pause : R.drawable.ic_play);
    }

//    @Subscribe
//    public void onPlayerServiceConnected(LocalPlayerServiceConnectedEvent event) {
//        player = event.getPlayer();
//        if (player.hasCurrentSong()) {
//            setCurrentSongInfo();
//            setPlayButton(player.isPlaying());
//        }
//    }

}
