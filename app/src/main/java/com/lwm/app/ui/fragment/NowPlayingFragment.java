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
import com.lwm.app.service.MusicService;
import com.lwm.app.ui.activity.LocalPlaybackActivity;
import com.lwm.app.ui.async.AlbumArtAsyncGetter;

public class NowPlayingFragment extends Fragment {

    private ImageView albumArt;
    private ImageView playPauseButton;
    private TextView artist;
    private TextView title;

    View.OnClickListener onPlayPauseClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LocalPlayer player = App.getMusicService().getLocalPlayer();
            player.togglePause();
            setPlayButton(player.isPlaying());
        }
    };

    View.OnClickListener onNowPlayingBarClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), LocalPlaybackActivity.class);
            startActivity(intent);
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
        if(App.isMusicServiceBound()){
            setCurrentSongInfo();
        }
    }

    public void setCurrentSongInfo(){
        if(App.getMusicService().getCurrentPlayerType() == MusicService.PLAYER_LOCAL) {
            if (LocalPlayer.hasCurrentSong()) {
                Song song = LocalPlayer.getCurrentSong();
                setAlbumArtFromUri(song.getAlbumArtUri());
                artist.setText(song.getArtist());
                title.setText(song.getTitle());
            }
        }
    }

    public void setAlbumArtFromUri(Uri uri){
        new AlbumArtAsyncGetter(getActivity(), albumArt).execute(uri);
    }

    public void setPlayButton(boolean playing){
        playPauseButton.setImageResource(playing? R.drawable.ic_pause : R.drawable.ic_play);
    }
}
