package com.lwm.app.ui.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lwm.app.App;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.service.MusicService;

public class PendingIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(App.isMusicServiceBound()) {
            MusicService musicService = App.getMusicService();
            LocalPlayer player = musicService.getLocalPlayer();
            if(player.hasCurrentSong()) {
                switch (intent.getAction()) {
                    case NowPlayingNotification.ACTION_CLOSE:
                        musicService.stopLocalPlayer();
                        NowPlayingNotification.hide();
                        break;
                    case NowPlayingNotification.ACTION_PREV:
                        player.prevSong();
                        new NowPlayingNotification(context).show();
                        break;
                    case NowPlayingNotification.ACTION_PLAY_PAUSE:
                        player.togglePause();
                        new NowPlayingNotification(context).show();
                        break;
                    case NowPlayingNotification.ACTION_NEXT:
                        player.nextSong();
                        new NowPlayingNotification(context).show();
                        break;
                }
            }
        }
    }

}
