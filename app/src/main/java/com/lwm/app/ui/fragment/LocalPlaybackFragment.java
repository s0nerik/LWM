package com.lwm.app.ui.fragment;

import android.widget.SeekBar;

import com.lwm.app.App;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.server.StreamServer;

public class LocalPlaybackFragment extends PlaybackFragment {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            // TODO: make seekTo available in streaming mode
            if(App.localPlayerActive() && !StreamServer.hasClients()){
                LocalPlayer player = App.getLocalPlayer();
                player.seekTo((int)((progress/100.)*player.getDuration()));
            }
        }
    }

}
