package app.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import app.Injector
import app.events.player.playback.control.ControlButtonEvent
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

import static android.content.Intent.ACTION_MEDIA_BUTTON
import static android.content.Intent.EXTRA_KEY_EVENT
import static android.view.KeyEvent.ACTION_DOWN
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
import static app.events.player.playback.control.ControlButtonEvent.Type.NEXT
import static app.events.player.playback.control.ControlButtonEvent.Type.PREV
import static app.events.player.playback.control.ControlButtonEvent.Type.TOGGLE_PAUSE

@CompileStatic
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Inject
    @PackageScope
    Bus bus

    MediaButtonIntentReceiver() {
        Injector.inject(this)
    }

    @Override
    void onReceive(Context context, Intent intent) {
        if (intent.action == ACTION_MEDIA_BUTTON) {
            def keyEvent = intent.extras.get(EXTRA_KEY_EVENT) as KeyEvent
            assert keyEvent : "keyEvent == null"
            if (keyEvent.action == ACTION_DOWN) {
                switch (keyEvent.keyCode) {
                    case KEYCODE_MEDIA_NEXT:
                        bus.post new ControlButtonEvent(NEXT)
                        break
                    case KEYCODE_MEDIA_PREVIOUS:
                        bus.post new ControlButtonEvent(PREV)
                        break
                    case KEYCODE_MEDIA_PLAY:
                    case KEYCODE_MEDIA_PAUSE:
                    case KEYCODE_MEDIA_PLAY_PAUSE:
                        bus.post new ControlButtonEvent(TOGGLE_PAUSE)
                        break
                }
            }
//            abortBroadcast();
        }
    }

}
