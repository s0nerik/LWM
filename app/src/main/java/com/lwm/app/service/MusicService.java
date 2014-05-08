package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;

public class MusicService extends Service {

    private LocalPlayer localPlayer;
    private StreamPlayer streamPlayer;

    private final MusicServiceBinder binder = new MusicServiceBinder();

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

}
