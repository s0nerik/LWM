package com.lwm.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lwm.app.ui.notification.NowPlayingNotification;

public class NotificationIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(NowPlayingNotification.ACTION_SHOW.equals(intent.getAction())){
            new NowPlayingNotification(context).show();
        }
    }

}