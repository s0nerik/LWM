package com.lwm.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lwm.app.App;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.notification.NowPlayingNotification;

public class PendingIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(App.isLocalPlayerServiceBound()) {
            LocalPlayerService player = App.getLocalPlayerService();

            if(App.localPlayerActive()){
                if(player.hasCurrentSong()) {
                    switch (intent.getAction()) {
                        case NowPlayingNotification.ACTION_CLOSE:
                            player.stopSelf();
                            NowPlayingNotification.hide();
                            break;
                        case NowPlayingNotification.ACTION_PREV:
                            player.prevSong();
                            break;
                        case NowPlayingNotification.ACTION_PLAY_PAUSE:
                            player.togglePause();
                            break;
                        case NowPlayingNotification.ACTION_NEXT:
                            player.nextSong();
                            break;
                    }
                } else {
                    NowPlayingNotification.hide();
                }
            } else {
                NowPlayingNotification.hide();
            }

        } else {
            NowPlayingNotification.hide();
        }
    }

}
