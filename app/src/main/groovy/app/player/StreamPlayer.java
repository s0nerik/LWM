package app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;

import java.io.IOException;

import javax.inject.Inject;

import app.Injector;
import app.events.client.SendReadyEvent;
import app.model.Song;
import app.server.StreamServer;
import ru.noties.debug.Debug;

public class StreamPlayer extends BasePlayer {

    @Inject Context context;

    private static boolean active = false;
    private Song currentSong;

    private Handler handler;

    @Inject Bus bus;

    public static final String STREAM_PATH = StreamServer.Url.STREAM;
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.d("LWM", "StreamPlayer: onPrepared");
            bus.post(new SendReadyEvent());
        }
    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            Debug.d("StreamPlayer: onSeekComplete");
            start();
        }
    };

    private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
//            Debug.d("Buffered: "+percent);
        }
    };

    public StreamPlayer() {
        super();
        Injector.inject(this);
        handler = new Handler(context.getMainLooper());
        setOnSeekCompleteListener(onSeekCompleteListener);
        setOnPreparedListener(onPreparedListener);
        setOnBufferingUpdateListener(onBufferingUpdateListener);
    }

    public void prepareNewSong(){
        reset();
        try {
            setDataSource(STREAM_PATH);
            prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nextSong() {
        Debug.d("StreamPlayer: nextSong");
        prepareNewSong();
    }

    @Override
    public void prevSong() {
        Debug.d("StreamPlayer: prevSong");
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
    public boolean isShuffle() {
        // TODO: return shuffle
        return false;
    }

    @Override
    public boolean isRepeat() {
        // TODO: return repeat
        return false;
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        stopNotifyingPlaybackProgress();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        updateSongInfo();
    }

    private void updateSongInfo() {
        // TODO: make it work
//        Ion.with(context)
//                .load(StreamServer.Url.CURRENT_INFO)
//                .as(Song.class)
//                .withResponse()
//                .setCallback(new FutureCallback<Response<Song>>() {
//                    @Override
//                    public void onCompleted(Exception e, Response<Song> result) {
//                        if (e == null) {
//                            setCurrentSong(result.getResult());
//                            bus.post(new PlaybackStartedEvent(result.getResult(), getCurrentPosition()));
//                            startNotifyingPlaybackProgress();
//                        } else {
//                            Debug.e("Error getting song info", e);
//                        }
//                    }
//                });
    }


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