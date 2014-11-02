package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.lwm.app.R;
import com.lwm.app.service.StreamPlayerService;

import javax.inject.Inject;

public class RemotePlaybackFragment extends PlaybackFragment {

    @Inject
    StreamPlayerService player;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mControls.setVisibility(View.GONE);
        mSeekBar.setEnabled(false);
        mSeekBar.setBackgroundResource(R.drawable.background_seekbar_no_controls);
    }

}
