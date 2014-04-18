package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.lwm.app.App;
import com.lwm.app.player.LocalPlayer;

public class LocalPlaybackFragment extends PlaybackFragment {

    LocalPlayer player;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = App.getMusicService().getLocalPlayer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            if(App.isMusicServiceBound()){
                player.seekTo((int)((progress/100.)*player.getDuration()));
            }
        }
    }

}
