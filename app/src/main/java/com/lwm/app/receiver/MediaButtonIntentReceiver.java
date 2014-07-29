package com.lwm.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.lwm.app.App;
import com.lwm.app.service.LocalPlayerService;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(App.localPlayerActive() && Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            assert keyEvent != null : "keyEvent == null";
            LocalPlayerService player = App.getLocalPlayerService();
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        player.nextSong();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        player.prevSong();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        player.togglePause();
                        break;
                }
            }
            abortBroadcast();
        }
    }

}
