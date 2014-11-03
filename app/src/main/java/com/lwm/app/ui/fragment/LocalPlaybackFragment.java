package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.lwm.app.R;
import com.lwm.app.events.access_point.AccessPointStateEvent;
import com.lwm.app.events.player.RepeatStateChangedEvent;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.SongChangedEvent;
import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.lib.WifiApManager;
import com.squareup.otto.Subscribe;

public class LocalPlaybackFragment extends PlaybackFragment {

    private View broadcastProgress;
    private ImageView broadcastIcon;
    private View chatButton;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSeekBar.setOnSeekBarChangeListener(new PlayerProgressOnSeekBarChangeListener());
        initToolbar();
    }

    private void initToolbar() {
        mToolbar.inflateMenu(R.menu.local_playback);

        View broadcastButton = mToolbar.findViewById(R.id.action_broadcast);
        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WifiAP().toggleWiFiAP();
            }
        });

        chatButton = mToolbar.findViewById(R.id.action_chat);

        broadcastProgress = broadcastButton.findViewById(R.id.progress);
        broadcastIcon = (ImageView) broadcastButton.findViewById(R.id.icon);

        if (new WifiApManager(getActivity()).getWifiApState() == WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED) {
            setBroadcastButtonEnabled(true);
        } else {
            setBroadcastButtonEnabled(false);
        }

    }

    private void showBroadcastProgressBar(boolean show) {
        broadcastProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        broadcastIcon.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setBroadcastButtonEnabled(boolean enabled) {
        broadcastIcon.setColorFilter(getResources().getColor(enabled? R.color.orange_main : android.R.color.white));
        chatButton.setVisibility(enabled? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void onAccessPointStateChanged(AccessPointStateEvent event) {
        switch (event.getState()) {
            case CHANGING:
                showBroadcastProgressBar(true);
                break;
            case DISABLED:
                showBroadcastProgressBar(false);
                setBroadcastButtonEnabled(false);
                break;
            case ENABLED:
                showBroadcastProgressBar(false);
                setBroadcastButtonEnabled(true);
                break;
        }
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
    public void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
        super.onCurrentSongAvailable(event);
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
