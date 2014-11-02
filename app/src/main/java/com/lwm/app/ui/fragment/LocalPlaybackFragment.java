package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.lwm.app.events.player.playback.SongPlayingEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.squareup.otto.Subscribe;

public class LocalPlaybackFragment extends PlaybackFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSeekBar.setOnSeekBarChangeListener(new PlayerProgressOnSeekBarChangeListener());
    }

    @Override
    @Subscribe
    public void onCurrentSongAvailable(CurrentSongAvailableEvent event) {
        super.onCurrentSongAvailable(event);
    }

    @Override
    @Subscribe
    public void onSongPlaying(SongPlayingEvent event) {
        super.onSongPlaying(event);
    }
}
