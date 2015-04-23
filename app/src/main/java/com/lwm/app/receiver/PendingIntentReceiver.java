package com.lwm.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lwm.app.Injector;
import com.lwm.app.events.player.playback.control.ChangeSongEvent;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.notification.NowPlayingNotification;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class PendingIntentReceiver extends BroadcastReceiver {

    @Inject
    Bus bus;

    public PendingIntentReceiver() {
        super();
        Injector.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case NowPlayingNotification.ACTION_CLOSE:
                context.stopService(new Intent(context, LocalPlayerService.class));
                break;
            case NowPlayingNotification.ACTION_PREV:
                bus.post(new ChangeSongEvent(ChangeSongEvent.Type.PREV));
                break;
            case NowPlayingNotification.ACTION_PLAY_PAUSE:
                bus.post(new ChangeSongEvent(ChangeSongEvent.Type.TOGGLE_PAUSE));
                break;
            case NowPlayingNotification.ACTION_NEXT:
                bus.post(new ChangeSongEvent(ChangeSongEvent.Type.NEXT));
                break;
        }
    }

}
