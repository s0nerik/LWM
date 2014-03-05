package com.lwm.app.task;

import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;

import com.lwm.app.fragment.PlaybackFragment;
import com.lwm.app.model.MusicPlayer;
import com.lwm.app.model.StreamPlayer;

import java.util.TimerTask;

public class SeekBarUpdateTask extends TimerTask {

    private int duration;
    private MediaPlayer player;
    private PlaybackFragment playbackFragment;
    private FragmentActivity parentActivity;
    private boolean isStream = false;

    public <T extends MediaPlayer> SeekBarUpdateTask(PlaybackFragment playbackFragment, T player, int duration){
        this.playbackFragment = playbackFragment;
        this.player = player;
        if(player instanceof StreamPlayer){
            isStream = true;
        }
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
                if(isStream){
                    playbackFragment.setCurrentTime(((StreamPlayer) player).getCurrentPositionInMinutes());
                }else{
                    playbackFragment.setCurrentTime(((MusicPlayer) player).getCurrentPositionInMinutes());
                }
            }
        });
    }
}
