package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.service.StreamPlayerService;

public class RemotePlaybackFragment extends PlaybackFragment {

    private StreamPlayerService player;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = App.getStreamPlayerService();
        playbackControls.setVisibility(View.GONE);
        seekBar.setEnabled(false);
        seekBar.setBackgroundResource(R.drawable.background_seekbar_no_controls);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        if(fromUser){
//            if(App.isLocalPlayerServiceBound()){
//                player.seekTo((int)((progress/100.)*player.getDuration()));
//            }
//        }
    }

    public void showWaitingFrame(boolean show){
        if(show) {
            waitingForStation.setVisibility(View.VISIBLE);
//            Uri video = Uri.parse("android.resource://com.lwm.app/raw/wait");
//            waitingForStation.setVideoURI(video);
//            waitingForStation.requestFocus();
//            waitingForStation.start();
        }else{
//            waitingForStation.stopPlayback();
            waitingForStation.setVisibility(View.GONE);
        }
    }

}
