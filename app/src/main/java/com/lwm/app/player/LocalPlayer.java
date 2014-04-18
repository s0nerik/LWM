package com.lwm.app.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.model.Song;
import com.lwm.app.server.ClientsStateListener;
import com.lwm.app.server.StreamServer;
import com.lwm.app.server.async.ClientsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LocalPlayer extends BasePlayer implements ClientsStateListener {

    private Context context;

    private boolean shuffle;
    private boolean repeat;

    private boolean active;
    private int currentQueuePosition;
    private int currentIndex = 0;
    private Song currentSong;
    private List<Song> queue = new ArrayList<>();

    private List<Integer> indexes = new ArrayList<>();

    private Random generator = new Random();

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
        active = false;
        currentQueuePosition = -1;
        setOnCompletionListener(onCompletionListener);
        setShuffle(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("shuffle", false));
    }

    public LocalPlayer(Context context, List<Song> playlist){
        this.context = context;
        active = false;
        currentQueuePosition = -1;
        queue = playlist;
        addIndexes(playlist.size());
        setOnCompletionListener(onCompletionListener);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setShuffle(sharedPreferences.getBoolean("shuffle", false));
        setRepeat(sharedPreferences.getBoolean("repeat", false));
    }

    public List<Song> getQueue(){
        return queue;
    }

    public void addToQueue(List<Song> songs){
        queue.addAll(songs);
        addIndexes(songs.size());
    }

    public void addToQueue(Song song){
        queue.add(song);
        addIndexes(1);
    }

    public Song getCurrentSong(){
        Log.d(App.TAG, "getCurrentSong");

        return currentSong;
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

            if(isListenerAttached()){
                playbackListener.onSongChanged(currentSong);
            }

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

        if(currentIndex+1 < indexes.size()) {
            currentQueuePosition = indexes.get(++currentIndex);
        }else{
            Toast t = Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT);
            t.show();
        }

        play(currentQueuePosition);

    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean flag) {
        shuffle = flag;
        if(shuffle){
            Collections.shuffle(indexes);
            currentIndex = 0;
        }else if(!indexes.isEmpty()){
            currentIndex = indexes.get(currentIndex);
            Collections.sort(indexes);
        }else{
            currentIndex = 0;
        }

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

        if(currentIndex-1 > 0) {
            currentQueuePosition = indexes.get(--currentIndex);
        }else{
            Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
            t.show();
        }

        play(currentQueuePosition);

    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if(StreamServer.hasClients()){
            new ClientsManager(this).pause();
        }
        if(isListenerAttached()) {
            playbackListener.onPlaybackPaused();
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if(StreamServer.hasClients()){
            ClientsManager manager = new ClientsManager(this);
            manager.start();
            try {
                Thread.sleep(manager.getClientsMaxPing());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.start();
        if(isListenerAttached()) {
            playbackListener.onPlaybackStarted();
        }
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    private void addIndexes(int n){
        int x = indexes.size();
        for(int i=x;i<x+n;i++){
            indexes.add(i);
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
    public void onWaitClients() {

    }
}