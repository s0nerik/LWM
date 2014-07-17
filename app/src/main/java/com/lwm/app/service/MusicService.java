package com.lwm.app.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;

import com.lwm.app.App;
import com.lwm.app.event.notification.HideNowPlayingNotificationEvent;
import com.lwm.app.event.notification.ShowNowPlayingNotificationEvent;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.receiver.MediaButtonIntentReceiver;
import com.lwm.app.ui.notification.NowPlayingNotification;
import com.squareup.otto.Subscribe;

public class MusicService extends Service {

    private LocalPlayer localPlayer;
    private StreamPlayer streamPlayer;

    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;

    private final MusicServiceBinder binder = new MusicServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        App.getEventBus().register(this);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
    }

    @Override
    public void onDestroy() {
        App.getEventBus().unregister(this);
        mAudioManager.unregisterMediaButtonEventReceiver(
                mRemoteControlResponder);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setLocalPlayer(LocalPlayer player){
        if(localPlayer != null && localPlayer.isPlaying()){
            stopLocalPlayer();
        }
        localPlayer = player;
    }

    public LocalPlayer getLocalPlayer(){
        if(streamPlayer != null && streamPlayer.isPlaying()){
            stopStreamPlayer();
        }

        return localPlayer;
    }

    public boolean localPlayerActive(){
        return localPlayer != null;
    }

    public boolean streamPlayerActive(){
        return streamPlayer != null;
    }

    public void stopLocalPlayer(){
        localPlayer.stop();
        localPlayer.release();
        localPlayer = null;
    }

    public void stopStreamPlayer(){
        streamPlayer.stop();
        streamPlayer.release();
        streamPlayer = null;
    }

    public StreamPlayer getStreamPlayer(){
        if(streamPlayer == null){
            streamPlayer = new StreamPlayer(this);
        }

        if(localPlayer != null && localPlayer.isPlaying()){
            stopLocalPlayer();
        }

        return streamPlayer;
    }

    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Subscribe
    public void showNowPlayingNotification (ShowNowPlayingNotificationEvent event) {
        new NowPlayingNotification(this).show();
    }

    @Subscribe
    public void hideNowPlayingNotification (HideNowPlayingNotificationEvent event) {
        NowPlayingNotification.hide();
    }

}
