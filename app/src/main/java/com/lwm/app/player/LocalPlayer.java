package com.lwm.app.player;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.event.player.PlaybackPausedEvent;
import com.lwm.app.event.player.PlaybackStartedEvent;
import com.lwm.app.event.player.PlaylistAddedToQueueEvent;
import com.lwm.app.event.player.QueueShuffledEvent;
import com.lwm.app.event.player.SongAddedToQueueEvent;
import com.lwm.app.model.Song;
import com.lwm.app.server.ClientsStateListener;
import com.lwm.app.server.StreamServer;
import com.lwm.app.server.async.ClientsManager;
import com.lwm.app.ui.notification.NowPlayingNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LocalPlayer extends BasePlayer implements ClientsStateListener {

    private Context context;

    private boolean shuffle = false;
    private boolean repeat = false;

    private boolean active = false;
    private int currentQueuePosition = -1;
    private Song currentSong;

    private List<Song> queue = new ArrayList<>();
    private int queueSize = 0;

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.d("LWM", "LocalPlayer: onCompletion");

            if(getCurrentPosition() > getDuration()-1000){
                if(isRepeat()){
                    play(currentQueuePosition);
                }else{
                    nextSong();
                }
            }
        }
    };

    public LocalPlayer(Context context){
        this.context = context;

        setOnCompletionListener(onCompletionListener);
    }

    public List<Song> getQueue(){
        return queue;
    }

    public void setQueue(List<Song> queue){
        this.queue = queue;
        queueSize = queue.size();
        shuffle = false;
    }

    public void shuffleQueue(){
        Collections.shuffle(queue);
        shuffle = true;
        currentQueuePosition = 0;
        play(currentQueuePosition);

        App.getEventBus().post(new QueueShuffledEvent(queue));
    }

    public void addToQueue(Collection<Song> songs){
        queue.addAll(songs);
        queueSize += songs.size();
        App.getEventBus().post(new PlaylistAddedToQueueEvent(queue));
    }

    public void addToQueue(Song song){
        queue.add(song);
        queueSize++;
        App.getEventBus().post(new SongAddedToQueueEvent(queue, song));
    }

    public Song getCurrentSong(){
        Log.d(App.TAG, "getCurrentSong");

        return currentSong;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    public void play(int position){

        currentQueuePosition = position;
        currentSong = queue.get(position);

        reset();
        try {
            assert queue != null : "queue == null";
            setDataSource(currentSong.getSource());
            prepare();

            active = true;

            if(StreamServer.hasClients()) {
                new ClientsManager(this).changeSong();
            }else{
                start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(App.TAG, "LocalPlayer: play("+position+")");
    }

    public boolean hasCurrentSong() {
        return active;
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "LocalPlayer: nextSong");

        if(currentQueuePosition+1 < queueSize) {
            play(++currentQueuePosition);
        }else{
            Toast t = Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT);
            t.show();
        }

    }

    public boolean isShuffle() {
        return shuffle;
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

        if(currentQueuePosition-1 >= 0) {
            play(--currentQueuePosition);
        }else{
            Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
            t.show();
        }

    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if(StreamServer.hasClients()){
            new ClientsManager(this).pause();
        }

        App.getEventBus().post(new PlaybackPausedEvent(currentSong, getCurrentPosition()));

        context.sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);

    }

    @Override
    public void start() throws IllegalStateException {
        if(StreamServer.hasClients()){
            ClientsManager manager = new ClientsManager(this);
            manager.start();
            try {
                long maxPing = manager.getClientsMaxPing();
                Thread.sleep(maxPing);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.start();

        App.getEventBus().post(new PlaybackStartedEvent(currentSong, getCurrentPosition()));

        context.sendOrderedBroadcast(new Intent(NowPlayingNotification.ACTION_SHOW), null);

    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    public int getCurrentQueuePosition() {
        return currentQueuePosition;
    }

    @Override
    public void onClientsReady() {
        start();
    }

    @Override
    public void onWaitClients() {}

    public int getQueueSize() {
        return queueSize;
    }
}