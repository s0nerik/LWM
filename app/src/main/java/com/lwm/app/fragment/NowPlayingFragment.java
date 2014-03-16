package com.lwm.app.fragment;

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
import com.lwm.app.async.AlbumArtAsyncGetter;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;

public class NowPlayingFragment extends Fragment {

    private ImageView albumArt;
    private ImageView playPauseButton;
    private TextView artist;
    private TextView title;

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
        title.setSelected(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(App.isMusicServiceBound()){
            setCurrentAlbumArt();
        }
    }

//    @Override
//    public void onServiceConnected(ComponentName className, IBinder service) {
//        super.onServiceConnected(className, service);
////        if(musicServiceBound){
//            LocalPlayer player = musicService.getLocalPlayer();
//            if(player != null){
//                Song song = player.getCurrentSong();
//                setAlbumArtFromUri(song.getAlbumArtUri());
//            }
////        }
//    }

    public void setCurrentAlbumArt(){
        LocalPlayer player = App.getMusicService().getLocalPlayer();
        if(player != null && player.getPlaylist() != null){
            Song song = player.getCurrentSong();
            setAlbumArtFromUri(song.getAlbumArtUri());
        }
    }

    public void setAlbumArtFromUri(Uri uri){
        new AlbumArtAsyncGetter(getActivity(), albumArt).execute(uri);
    }

    public void setArtist(String artist) {
        this.artist.setText(artist);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setPlayButton(boolean playing){
        playPauseButton.setImageResource(playing? R.drawable.ic_pause : R.drawable.ic_play);
    }
}
