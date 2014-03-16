package com.lwm.app.task;

import android.support.v4.app.FragmentActivity;

import com.lwm.app.fragment.PlaybackFragment;
import com.lwm.app.player.BasePlayer;

import java.util.TimerTask;

public class SeekBarUpdateTask extends TimerTask {

    private int duration;
    private BasePlayer player;
    private PlaybackFragment playbackFragment;
    private FragmentActivity parentActivity;

    public SeekBarUpdateTask(PlaybackFragment playbackFragment, BasePlayer player, int duration){
        this.playbackFragment = playbackFragment;
        this.player = player;
        parentActivity = playbackFragment.getActivity();
        this.duration = duration;
    }

    int progress;
    @Override
    public void run() {
        progress = (int) (player.getCurrentPosition()/(float) duration * PlaybackFragment.SEEK_BAR_MAX);
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playbackFragment.setSeekBarPosition(progress);
                playbackFragment.setCurrentTime(player.getCurrentPositionInMinutes());
            }
        });
    }
}
