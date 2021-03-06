package app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import app.App
import app.events.player.playback.control.ControlButtonEvent
import com.github.s0nerik.rxbus.RxBus
import groovy.transform.CompileStatic

import static android.content.Intent.ACTION_MEDIA_BUTTON
import static android.content.Intent.EXTRA_KEY_EVENT
import static android.view.KeyEvent.*
import static app.events.player.playback.control.ControlButtonEvent.Type.NEXT
import static app.events.player.playback.control.ControlButtonEvent.Type.PREV
import static app.events.player.playback.control.ControlButtonEvent.Type.TOGGLE_PAUSE

@CompileStatic
class MediaButtonIntentReceiver extends BroadcastReceiver {

    MediaButtonIntentReceiver() {
        App.get().inject(this)
    }

    @Override
    void onReceive(Context context, Intent intent) {
        if (intent.action == ACTION_MEDIA_BUTTON) {
            def keyEvent = intent.extras.get(EXTRA_KEY_EVENT) as KeyEvent
            assert keyEvent : "keyEvent == null"
            if (keyEvent.action == ACTION_DOWN) {
                switch (keyEvent.keyCode) {
                    case KEYCODE_MEDIA_NEXT:
                        RxBus.post new ControlButtonEvent(NEXT)
                        break
                    case KEYCODE_MEDIA_PREVIOUS:
                        RxBus.post new ControlButtonEvent(PREV)
                        break
                    case KEYCODE_MEDIA_PLAY:
                    case KEYCODE_MEDIA_PAUSE:
                    case KEYCODE_MEDIA_PLAY_PAUSE:
                        RxBus.post new ControlButtonEvent(TOGGLE_PAUSE)
                        break
                }
            }
//            abortBroadcast();
        }
    }

}
