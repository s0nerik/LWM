package app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.App
import app.events.player.playback.control.ControlButtonEvent
import app.rx.RxBus
import app.services.LocalPlayerService
import groovy.transform.CompileStatic

import static app.events.player.playback.control.ControlButtonEvent.Type.NEXT
import static app.events.player.playback.control.ControlButtonEvent.Type.PREV
import static app.events.player.playback.control.ControlButtonEvent.Type.TOGGLE_PAUSE
import static app.ui.notification.NowPlayingNotification.ACTION_CLOSE
import static app.ui.notification.NowPlayingNotification.ACTION_NEXT
import static app.ui.notification.NowPlayingNotification.ACTION_PLAY_PAUSE
import static app.ui.notification.NowPlayingNotification.ACTION_PREV

@CompileStatic
class PendingIntentReceiver extends BroadcastReceiver {

    PendingIntentReceiver() {
        super()
        App.get().inject(this)
    }

    @Override
    void onReceive(Context context, Intent intent) {
        switch (intent.action) {
            case ACTION_CLOSE:
                context.stopService new Intent(context, LocalPlayerService)
                break
            case ACTION_PREV:
                RxBus.post new ControlButtonEvent(PREV)
                break
            case ACTION_PLAY_PAUSE:
                RxBus.post new ControlButtonEvent(TOGGLE_PAUSE)
                break
            case ACTION_NEXT:
                RxBus.post new ControlButtonEvent(NEXT)
                break
        }
    }

}
