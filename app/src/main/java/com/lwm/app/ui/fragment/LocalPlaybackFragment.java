package com.lwm.app.ui.fragment;

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
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import static com.lwm.app.events.access_point.AccessPointStateEvent.State.ENABLED;

public class LocalPlaybackFragment extends PlaybackFragment {

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
