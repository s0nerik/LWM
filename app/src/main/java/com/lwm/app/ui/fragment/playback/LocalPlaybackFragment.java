package com.lwm.app.ui.fragment.playback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.events.access_point.AccessPointStateEvent;
import com.lwm.app.events.player.RepeatStateChangedEvent;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.SongChangedEvent;
import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.helper.wifi.WifiAP;
import com.lwm.app.player.BasePlayer;
import com.lwm.app.player.LocalPlayer;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.OnClick;

import static com.lwm.app.events.access_point.AccessPointStateEvent.State.ENABLED;

public class LocalPlaybackFragment extends PlaybackFragment {

    @Inject
    LocalPlayer player;

    @Inject
    WifiAP wifiAP;

    private View chatButton;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSeekBar.setOnSeekBarChangeListener(new PlayerProgressOnSeekBarChangeListener());
        initToolbar();
    }

    private void initToolbar() {
        mToolbar.inflateMenu(R.menu.playback_local);
        chatButton = mToolbar.findViewById(R.id.action_chat);
        setChatButtonVisibility(wifiAP.isEnabled());
    }

    private void setChatButtonVisibility(boolean show) {
        chatButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnClick({R.id.btnPlayPause, R.id.btnNext, R.id.btnPrev, R.id.btnShuffle, R.id.btnRepeat})
    public void onClickControls(View btn) {
        switch (btn.getId()) {
            case R.id.btnNext:
                player.nextSong();
                break;
            case R.id.btnPrev:
                player.prevSong();
                break;
            case R.id.btnPlayPause:
                player.togglePause();
                break;
            case R.id.btnShuffle:
                player.shuffleQueueExceptPlayed();
                break;
            case R.id.btnRepeat:
                player.setRepeat(!player.isRepeat());
                break;
        }
    }

    @Override
    protected BasePlayer getPlayer() {
        return player;
    }

    @Subscribe
    public void onAccessPointStateChanged(AccessPointStateEvent event) {
        setChatButtonVisibility(event.getState() == ENABLED);
    }

    @Subscribe
    @Override
    public void onSongChanged(SongChangedEvent event) {
        super.onSongChanged(event);
    }

    @Subscribe
    @Override
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        super.onPlaybackStarted(event);
    }

    @Subscribe
    @Override
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        super.onPlaybackPaused(event);
    }

    @Subscribe
    @Override
    public void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event);
    }

    @Subscribe
    @Override
    public void onQueueShuffled(QueueShuffledEvent event) {
        super.onQueueShuffled(event);
    }

    @Subscribe
    @Override
    public void onRepeatStateChanged(RepeatStateChangedEvent event) {
        super.onRepeatStateChanged(event);
    }
}
