package com.lwm.app.ui.fragment;

import android.widget.SeekBar;

import com.lwm.app.App;
import com.lwm.app.service.LocalPlayerService;

public class LocalPlaybackFragment extends PlaybackFragment {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            if(App.localPlayerActive()){
                LocalPlayerService player = App.getLocalPlayerService();
                player.seekTo((int)((progress/100.)*player.getDuration()));
            }
        }
    }

}
