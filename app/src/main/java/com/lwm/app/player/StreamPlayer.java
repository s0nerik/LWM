package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.events.player.PlaybackPausedEvent;
import com.lwm.app.events.player.PlaybackStartedEvent;
import com.lwm.app.model.Song;
import com.lwm.app.server.StreamServer;

import java.io.IOException;

public class StreamPlayer extends BasePlayer {

    private Context context;
    private static boolean active = false;
    private Song currentSong;

    private Handler handler;

//    private File tempFile;

    public static final String STREAM_PATH = StreamServer.Url.STREAM;
//    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
//        @Override
//        public void onPrepared(MediaPlayer mediaPlayer) {
//            Log.d("LWM", "StreamPlayer: onPrepared");
//            new ReadinessReporter(StreamPlayer.this).execute();
//        }
//    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            start();
        }
    };

    public StreamPlayer(Context context) {
        super(context);
        this.context = context;
        handler = new Handler(context.getMainLooper());
        setOnSeekCompleteListener(onSeekCompleteListener);

//        tempFile = new File(context.getCacheDir(), "song.mp3");

//        setOnPreparedListener(onPreparedListener);
    }

//    public void register(){
//        active = true;
//        Ion.with(context)
//                .load(StreamServer.Url.CLIENT_REGISTER)
//                .noCache()
//                .setLogging(App.TAG, Log.DEBUG)
//                .setStringBody("")
//                .asString();
//    }

    public void prepareNewSong(){
        reset();
        try {
            setDataSource(STREAM_PATH);
//            setDataSource(tempFile.getPath());
            prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "StreamPlayer: nextSong");
        prepareNewSong();
    }

    @Override
    public void prevSong() {
        Log.d(App.TAG, "StreamPlayer: prevSong");
        prepareNewSong();
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        handler.post(new Runnable() {
            @Override
            public void run() {
                App.getBus().post(new PlaybackPausedEvent(currentSong, getCurrentPosition()));
            }
        });
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        handler.post(new Runnable() {
            @Override
            public void run() {
                App.getBus().post(new PlaybackStartedEvent(currentSong, getCurrentPosition()));
            }
        });
    }

//    public File getTempFile() {
//        return tempFile;
//    }

//    public void unregister(){
//        Ion.with(context)
//                .load(StreamServer.Url.CLIENT_UNREGISTER)
//                .noCache()
//                .setLogging(App.TAG, Log.DEBUG)
//                .setStringBody("")
//                .asString();
////        new ClientUnregister().execute();
//    }

    public static boolean isActive(){
        return active;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }
}