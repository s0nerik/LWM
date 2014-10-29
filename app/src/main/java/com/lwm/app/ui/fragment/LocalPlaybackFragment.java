package com.lwm.app.ui.fragment;

import android.widget.SeekBar;

import com.lwm.app.events.player.service.LocalPlayerServiceConnectedEvent;
import com.lwm.app.player.LocalPlayer;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class LocalPlaybackFragment extends PlaybackFragment {

    @Inject
    LocalPlayer player;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (player != null && fromUser) {
            player.seekTo((int) ((progress / 100.) * player.getDuration()));
        }
    }

    @Subscribe
    public void onPlayerServiceConnected(LocalPlayerServiceConnectedEvent event) {
        player = event.getPlayer();
    }

}
