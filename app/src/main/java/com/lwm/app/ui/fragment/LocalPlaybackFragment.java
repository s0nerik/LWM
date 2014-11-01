package com.lwm.app.ui.fragment;

import android.widget.SeekBar;

public class LocalPlaybackFragment extends PlaybackFragment {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (player != null && fromUser) {
            player.seekTo((int) ((progress / 100.) * player.getDuration()));
        }
    }

}
