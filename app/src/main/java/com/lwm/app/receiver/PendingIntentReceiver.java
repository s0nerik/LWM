package com.lwm.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lwm.app.App;
import com.lwm.app.events.player.StopForegroundLocalPlayerEvent;
import com.lwm.app.events.player.binding.UnbindLocalPlayerServiceEvent;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.notification.NowPlayingNotification;

public class PendingIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(App.isLocalPlayerServiceBound()) {
            LocalPlayerService player = App.getLocalPlayerService();

            if(player.hasCurrentSong()) {
                switch (intent.getAction()) {
                    case NowPlayingNotification.ACTION_CLOSE:
                        App.getBus().post(new StopForegroundLocalPlayerEvent());
                        App.getBus().post(new UnbindLocalPlayerServiceEvent());
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
                App.getBus().post(new StopForegroundLocalPlayerEvent());
            }

        } else {
            App.getBus().post(new StopForegroundLocalPlayerEvent());
        }
    }

}
