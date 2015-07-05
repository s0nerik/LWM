package app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import app.Injector;
import app.events.player.playback.control.ChangeSongEvent;
import app.service.LocalPlayerService;
import app.ui.notification.NowPlayingNotification;

public class PendingIntentReceiver extends BroadcastReceiver {

    @Inject
    Bus bus;

    public PendingIntentReceiver() {
        super();
        Injector.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        switch (intent.getAction()) {
//            case NowPlayingNotification.ACTION_CLOSE:
//                context.stopService(new Intent(context, LocalPlayerService.class));
//                break;
//            case NowPlayingNotification.ACTION_PREV:
//                bus.post(new ChangeSongEvent(ChangeSongEvent.Type.PREV));
//                break;
//            case NowPlayingNotification.ACTION_PLAY_PAUSE:
//                bus.post(new ChangeSongEvent(ChangeSongEvent.Type.TOGGLE_PAUSE));
//                break;
//            case NowPlayingNotification.ACTION_NEXT:
//                bus.post(new ChangeSongEvent(ChangeSongEvent.Type.NEXT));
//                break;
//        }
        String s = intent.getAction();
        if (s.equals(NowPlayingNotification.ACTION_CLOSE)) {
            context.stopService(new Intent(context, LocalPlayerService.class));

        } else if (s.equals(NowPlayingNotification.ACTION_PREV)) {
            bus.post(new ChangeSongEvent(ChangeSongEvent.Type.PREV));

        } else if (s.equals(NowPlayingNotification.ACTION_PLAY_PAUSE)) {
            bus.post(new ChangeSongEvent(ChangeSongEvent.Type.TOGGLE_PAUSE));

        } else if (s.equals(NowPlayingNotification.ACTION_NEXT)) {
            bus.post(new ChangeSongEvent(ChangeSongEvent.Type.NEXT));

        }
    }

}
