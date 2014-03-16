package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;

public class MusicService extends Service {

//    public static final String ACTION_PLAY_SONG = "com.lwm.player.action.PLAY_SONG";
//    public static final String ACTION_PAUSE_SONG = "com.lwm.player.action.PAUSE_SONG";
//    public static final String ACTION_UNPAUSE_SONG = "com.lwm.player.action.UNPAUSE_SONG";
//    public static final String ACTION_NEXT_SONG = "com.lwm.player.action.NEXT_SONG";
//    public static final String ACTION_PREV_SONG = "com.lwm.player.action.PREV_SONG";
//    public static final String ACTION_SONG_SEEK_TO = "com.lwm.player.action.SONG_SEEK_TO";
//    public static final String ACTION_SHUFFLE_ON = "com.lwm.player.action.SHUFFLE_ON";
//    public static final String ACTION_SHUFFLE_OFF = "com.lwm.player.action.SHUFFLE_OFF";
//    public static final String ACTION_REPEAT_ON = "com.lwm.player.action.REPEAT_ON";
//    public static final String ACTION_REPEAT_OFF = "com.lwm.player.action.REPEAT_OFF";
//
//    public static final String ACTION_PLAY_STREAM = "com.lwm.player.action.PLAY_STREAM";
//    public static final String ACTION_STREAM_NEXT_SONG = "com.lwm.player.action.PLAY_STREAM";
//    public static final String ACTION_STREAM_PAUSE = "com.lwm.player.action.STREAM_PAUSE";
//    public static final String ACTION_STREAM_UNPAUSE = "com.lwm.player.action.STREAM_UNPAUSE";

    private LocalPlayer player;
    private StreamPlayer streamPlayer;

    private final MusicServiceBinder binder = new MusicServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        Log.d(App.TAG, "MusicService.onStartCommand");
//
////        String action = null;
////        if(intent != null){
////            action = intent.getAction();
////
////            switch(action){
////                case ACTION_PLAY_SONG:
////                    Log.d(App.TAG, "MusicService: ACTION_PLAY_SONG");
////                    int pos = intent.getIntExtra(BasePlayer.PLAYLIST_POSITION, -1);
////                    play(pos);
////                    sendBroadcast(new Intent(BasePlayer.PLAYBACK_STARTED));
////                    break;
////
////                case ACTION_PAUSE_SONG:
////                    Log.d(App.TAG, "MusicService: ACTION_PAUSE_SONG");
////                    player.pause();
////                    sendBroadcast(new Intent(BasePlayer.PLAYBACK_PAUSED));
////                    break;
////
////                case ACTION_UNPAUSE_SONG:
////                    Log.d(App.TAG, "MusicService: ACTION_UNPAUSE_SONG");
////                    player.start();
////                    sendBroadcast(new Intent(BasePlayer.PLAYBACK_STARTED));
////                    break;
////
////                case ACTION_SONG_SEEK_TO:
////                    Log.d(App.TAG, "MusicService: ACTION_SONG_SEEK_TO");
////                    int newPos = intent.getIntExtra(BasePlayer.SEEK_POSITION, -1);
////                    Log.d(App.TAG, "MusicService: seekTo("+newPos+")");
////                    player.seekTo(newPos);
////                    break;
////
////                case ACTION_NEXT_SONG:
////                    Log.d(App.TAG, "MusicService: ACTION_NEXT_SONG");
////                    player.nextSong();
////                    break;
////
////                case ACTION_PREV_SONG:
////                    Log.d(App.TAG, "MusicService: ACTION_PREV_SONG");
////                    player.prevSong();
////                    break;
////
////                case ACTION_PLAY_STREAM:
////                    Log.d(App.TAG, "MusicService: ACTION_PLAY_STREAM");
////                    playStream();
////                    break;
////            }
////
////        }
//        return Service.START_STICKY;
//    }

//    public void play(Playlist playlist, int pos){
//        if(streamPlayer != null && streamPlayer.isPlaying()){
//            streamPlayer.stop();
//            streamPlayer.release();
//            streamPlayer = null;
//        }
//
//        if(player == null){
//            player = new LocalPlayer(this, playlist);
//        }
//        player.play(pos);
//    }

//    private void play(int pos){
//        Log.d(App.TAG, "MusicService.play()");
//
//        if(streamPlayer != null && streamPlayer.isPlaying()){
//            streamPlayer.stop();
//            streamPlayer.release();
//            streamPlayer = null;
//        }
//
//        if(player == null){
//            player = new LocalPlayer(this, new SongsCursorGetter(this).getSongs());
//        }
//        player.play(pos);
//    }

//    private void playStream() {
//        Log.d(App.TAG, "MusicService.playStream()");
//
//        if(player != null && player.isPlaying()){
//            player.stop();
//            player.release();
//            player = null;
//        }
//
//        if(streamPlayer == null){
//            streamPlayer = new StreamPlayer(this);
//        }
//        streamPlayer.play();
//
//    }

    public void setLocalPlayer(LocalPlayer player){
        if(this.player != null){
            this.player.stop();
            this.player.release();
            this.player = null;
        }
        this.player = player;
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

        return player;
    }

    public StreamPlayer getStreamPlayer(){
        if(streamPlayer == null){
            streamPlayer = new StreamPlayer(this);
        }

        if(player != null && player.isPlaying()){
            player.stop();
            player.release();
            player = null;
        }

        return streamPlayer;
    }

    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

}
