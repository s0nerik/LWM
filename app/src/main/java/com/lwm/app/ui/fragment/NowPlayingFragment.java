package com.lwm.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.danh32.fontify.TextView;
import com.koushikdutta.ion.Ion;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.SongChangedEvent;
import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.BitmapInfoCallback;
import com.lwm.app.ui.activity.LocalPlaybackActivity;
import com.lwm.app.ui.base.DaggerFragment;
import com.lwm.app.ui.custom_view.ProgressWheel;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class NowPlayingFragment extends DaggerFragment {

    @Inject
    Utils utils;
    @Inject
    Bus bus;
    @Inject
    LocalPlayer player;

    @InjectView(R.id.cover)
    ImageView mCover;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.artist)
    TextView mArtist;
    @InjectView(R.id.layout)
    View mLayout;
    @InjectView(R.id.btn_play_pause)
    ImageView mBtnPlayPause;
    @InjectView(R.id.global_layout)
    RelativeLayout mGlobalLayout;
    @InjectView(R.id.progress_wheel)
    ProgressWheel mProgressWheel;

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        Song song = player.getCurrentSong();
        if (song != null) {
            setSongInfo(player.getCurrentSong());
        }
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void setSongInfo(Song song) {
        Ion.with(mCover)
                .smartSize(true)
                .placeholder(R.drawable.no_cover)
                .error(R.drawable.no_cover)
                .load(song.getAlbumArtUri().toString())
                .withBitmapInfo()
                .setCallback(new BitmapInfoCallback(mGlobalLayout, mTitle, mArtist));

        mArtist.setText(utils.getArtistName(song.getArtist()));
        mTitle.setText(song.getTitle());
        setPlayButton(player.isPlaying());

    }

    private void setPlayButton(boolean playing) {
        mBtnPlayPause.setImageResource(playing ? R.drawable.ic_av_pause : R.drawable.ic_av_play_arrow);
    }

    @OnClick(R.id.btn_play_pause)
    public void onPlayPauseClicked() {
        player.togglePause();
        setPlayButton(player.isPlaying());
    }

    @OnClick({R.id.layout, R.id.cover})
    public void onLayoutClicked() {
        Intent intent = new Intent(getActivity(), LocalPlaybackActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
    }

    @Subscribe
    public void onSongChanged(SongChangedEvent event) {
        setSongInfo(event.getSong());
    }

    @Subscribe
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        setPlayButton(true);
    }

    @Subscribe
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        setPlayButton(false);
    }

    @Subscribe
    public void onPlaying(SongPlayingEvent event) {
        mProgressWheel.setProgress((int) (360 * (event.getProgress() / (float) event.getDuration())));
    }

}
