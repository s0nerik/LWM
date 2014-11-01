package com.lwm.app.player;

import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.queue.PlaylistAddedToQueueEvent;
import com.lwm.app.events.player.queue.PlaylistRemovedFromQueueEvent;
import com.lwm.app.events.player.queue.QueueShuffledEvent;
import com.lwm.app.events.player.queue.SongAddedToQueueEvent;
import com.lwm.app.events.player.queue.SongRemovedFromQueueEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.PrepareClientsEvent;
import com.lwm.app.events.server.SeekToClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class LocalPlayer extends BasePlayer {

    private LocalPlayerService service;

    private boolean repeat = false;

    private boolean active = false;

    private Queue queue = new Queue();

    @Inject Bus bus;

    @Inject Context context;

    @Inject NotificationManager notificationManager;

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.d("LWM", "LocalPlayer: onCompletion");

            if(getCurrentPosition() > getDuration()-1000){
                if(isRepeat()){
                    play();
                }else{
                    nextSong();
                }
            }
        }
    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            start();
        }
    };

    public LocalPlayer(LocalPlayerService service){
        super();
        this.service = service;

        setOnCompletionListener(onCompletionListener);
        setOnSeekCompleteListener(onSeekCompleteListener);
    }

    public List<Song> getQueue(){
        return queue.getQueue();
    }

    public void setQueue(List<Song> songs){
        queue = new Queue(songs);
    }

    public void shuffleQueue(){
        queue.shuffle();
        bus.post(new QueueShuffledEvent(getQueue()));
    }

    public void shuffleQueueExceptPlayed(){
        queue.shuffleExceptPlayed();
        bus.post(new QueueShuffledEvent(getQueue()));
    }

    public void addToQueue(List<Song> songs){
        queue.addSongs(songs);
        bus.post(new PlaylistAddedToQueueEvent(getQueue(), songs));
    }

    public void addToQueue(Song song){
        queue.addSong(song);
        bus.post(new SongAddedToQueueEvent(getQueue(), song));
    }

    public void removeFromQueue(Song song){
        queue.removeSong(song);
        bus.post(new SongRemovedFromQueueEvent(getQueue(), song));
    }

    public void removeFromQueue(List<Song> songs){
        queue.removeSongs(songs);
        bus.post(new PlaylistRemovedFromQueueEvent(getQueue(), songs));
    }

    public Song getCurrentSong(){
        Log.d(App.TAG, "getCurrentSong");
        return queue.getSong();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    public void play(int position){
        queue.moveTo(position);
        play();
    }

    public void play(){
        reset();
        try {
            assert queue != null : "queue == null";
            setDataSource(queue.getSong().getSource());
            prepare();

            active = true;

            if (service.isServerStarted()) {
                bus.post(new PrepareClientsEvent());
            } else {
                start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCurrentSong() {
        return active;
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "LocalPlayer: nextSong");

        if(queue.moveToNext()) {
            play();
        } else {
            Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        Log.d(App.TAG, "LocalPlayer: seekTo("+msec+")");
        if (service.isServerStarted()) {
            bus.post(new SeekToClientsEvent(msec));
        }
        super.seekTo(msec);
    }

    public boolean isShuffle() {
        return queue.isShuffled();
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean flag) {
        repeat = flag;
    }

    @Override
    public void prevSong(){
        Log.d(App.TAG, "LocalPlayer: prevSong");

        if(queue.moveToPrev()) {
            play();
        } else {
            Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();

        bus.post(new PlaybackPausedEvent(queue.getSong(), getCurrentPosition()));
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();

        bus.post(new PlaybackStartedEvent(queue.getSong(), getCurrentPosition()));
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            if(service.isServerStarted()) {
                bus.post(new PauseClientsEvent());
            } else {
                pause();
            }
        } else {
            if(service.isServerStarted()) {
                bus.post(new StartClientsEvent());
            } else {
                start();
            }
        }
    }

    public int getCurrentQueuePosition() {
        return queue.getCurrentIndex();
    }

    public int getQueueSize() {
        return queue.getSize();
    }

    public boolean isSongInQueue(Song song) {
        return queue.contains(song);
    }

}