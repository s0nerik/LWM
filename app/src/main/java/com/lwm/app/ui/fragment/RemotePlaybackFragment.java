package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.service.StreamPlayerService;

public class RemotePlaybackFragment extends PlaybackFragment {

    StreamPlayerService player;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = App.getStreamPlayerService();
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
            getActivity().findViewById(R.id.waiting_for_station).setVisibility(View.VISIBLE);
        }else{
            getActivity().findViewById(R.id.waiting_for_station).setVisibility(View.GONE);
        }
    }

}
