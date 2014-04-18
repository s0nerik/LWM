package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;

public class MusicService extends Service {

    public static int PLAYER_LOCAL = 0;
    public static int PLAYER_STREAM = 1;

    private int currentPlayerType;

    private LocalPlayer player;
    private StreamPlayer streamPlayer;

    private final MusicServiceBinder binder = new MusicServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setLocalPlayer(LocalPlayer player){
        if(this.player != null){
            this.player.stop();
            this.player.release();
            this.player = null;
        }
        this.player = player;
        currentPlayerType = PLAYER_LOCAL;
    }

    public LocalPlayer getLocalPlayer(){
        if(player == null){
            player = new LocalPlayer(this);
        }

        if(streamPlayer != null && streamPlayer.isPlaying()){
            streamPlayer.stop();
            streamPlayer.release();
            streamPlayer = null;
        }

        currentPlayerType = PLAYER_LOCAL;
        return player;
    }

    public StreamPlayer getStreamPlayer(){
        if(streamPlayer == null){
            streamPlayer = new StreamPlayer(this);
        }

        if(player != null){
            if(player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }

        currentPlayerType = PLAYER_STREAM;
        return streamPlayer;
    }

    public int getCurrentPlayerType() {
        return currentPlayerType;
    }

    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

}
