package app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import app.Injector;
import app.events.player.playback.control.ChangeSongEvent;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Inject
    Bus bus;

    public MediaButtonIntentReceiver() {
        Injector.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            assert keyEvent != null : "keyEvent == null";
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        bus.post(new ChangeSongEvent(ChangeSongEvent.Type.NEXT));
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        bus.post(new ChangeSongEvent(ChangeSongEvent.Type.PREV));
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        bus.post(new ChangeSongEvent(ChangeSongEvent.Type.TOGGLE_PAUSE));
                        break;
                }
            }
//            abortBroadcast();
        }
    }

}
