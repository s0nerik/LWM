package app.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import app.Injector
import app.events.player.playback.control.ChangeSongEvent
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

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
        if (intent.action == Intent.ACTION_MEDIA_BUTTON) {
            def keyEvent = intent.extras.get(Intent.EXTRA_KEY_EVENT) as KeyEvent
            assert keyEvent : "keyEvent == null"
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                switch (keyEvent.keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        bus.post new ChangeSongEvent(ChangeSongEvent.Type.NEXT)
                        break
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        bus.post new ChangeSongEvent(ChangeSongEvent.Type.PREV)
                        break
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        bus.post new ChangeSongEvent(ChangeSongEvent.Type.TOGGLE_PAUSE)
                        break
                }
            }
//            abortBroadcast();
        }
    }

}
