package app.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.Injector
import app.events.player.playback.control.ControlButtonEvent
import app.services.LocalPlayerService
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

import static app.events.player.playback.control.ControlButtonEvent.Type.NEXT
import static app.events.player.playback.control.ControlButtonEvent.Type.PREV
import static app.events.player.playback.control.ControlButtonEvent.Type.TOGGLE_PAUSE
import static app.ui.notification.NowPlayingNotification.ACTION_CLOSE
import static app.ui.notification.NowPlayingNotification.ACTION_NEXT
import static app.ui.notification.NowPlayingNotification.ACTION_PLAY_PAUSE
import static app.ui.notification.NowPlayingNotification.ACTION_PREV

@CompileStatic
class PendingIntentReceiver extends BroadcastReceiver {

    @Inject
    @PackageScope
    Bus bus

    PendingIntentReceiver() {
        super()
        Injector.inject(this)
    }

    @Override
    void onReceive(Context context, Intent intent) {
        switch (intent.action) {
            case ACTION_CLOSE:
                context.stopService new Intent(context, LocalPlayerService)
                break
            case ACTION_PREV:
                bus.post new ControlButtonEvent(PREV)
                break
            case ACTION_PLAY_PAUSE:
                bus.post new ControlButtonEvent(TOGGLE_PAUSE)
                break
            case ACTION_NEXT:
                bus.post new ControlButtonEvent(NEXT)
                break
        }
    }

}
