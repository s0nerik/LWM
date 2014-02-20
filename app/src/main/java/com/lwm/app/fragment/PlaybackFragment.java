package com.lwm.app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.model.MusicPlayer;
import com.lwm.app.service.MusicService;

public class PlaybackFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    public static final int SEEK_BAR_MAX = 100;
    public static final int SEEK_BAR_UPDATE_INTERVAL = 1000;

    private TextView title;
    private TextView artist;
    private TextView currentTime;
    private TextView duration;
    private SeekBar seekBar;
    private ImageView albumArt;

    private ImageView playPauseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playback, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView) view.findViewById(R.id.fragment_playback_title);
        artist = (TextView) view.findViewById(R.id.fragment_playback_artist);
        duration = (TextView) view.findViewById(R.id.fragment_playback_last_position);
        currentTime = (TextView) view.findViewById(R.id.fragment_playback_now_position);
        seekBar = (SeekBar) view.findViewById(R.id.fragment_playback_seekBar);
        albumArt = (ImageView) view.findViewById(R.id.fragment_playback_cover);

        MusicPlayer player = MusicService.getCurrentPlayer();

        playPauseButton = (ImageView) view.findViewById(R.id.fragment_playback_play_pause);
        setPlayButton(player.isPlaying());

        seekBar.setMax(SEEK_BAR_MAX);
        seekBar.setOnSeekBarChangeListener(this);

        title.setText(MusicService.getCurrentPlayer().getCurrentTitle());
        artist.setText(MusicService.getCurrentPlayer().getCurrentArtist());
        duration.setText(MusicService.getCurrentPlayer().getCurrentDurationInMinutes());

        setAlbumArtFromUri(player.getCurrentAlbumArtUri());
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setArtist(String artist) {
        this.artist.setText(artist);
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime.setText(currentTime);
    }

    public void setDuration(String duration) {
        this.duration.setText(duration);
    }

    public void setSeekBarPosition(int percents) {
        this.seekBar.setProgress(percents);
    }

    public void setAlbumArtFromUri(Uri uri){
        albumArt.setImageURI(uri);
        if(albumArt.getDrawable() == null){
            setDefaultAlbumArt();
        }
    }

    public void setDefaultAlbumArt() {
        albumArt.setImageResource(R.drawable.ic_launcher);
    }

    public void setPlayButton(boolean playing){
        if(playing){
            playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.button_pause));
        }else{
            playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.button_play));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            getActivity().startService(new Intent(getActivity(), MusicService.class)
                    .setAction(MusicService.ACTION_SONG_SEEK_TO)
                    .putExtra(MusicPlayer.SEEK_POSITION, (int)((progress/100.0)*MusicService.getCurrentPlayer().getDuration())));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

}
