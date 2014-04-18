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
import java.util.List;
import java.util.Random;

public class LocalPlayer extends BasePlayer implements ClientsStateListener {

    private Context context;

    private static boolean active;
    private static int currentQueuePosition;
    private static Song currentSong;
    private static List<Song> queue = new ArrayList<>();

    private List<Integer> indexes = new ArrayList<>();

    private Random generator = new Random();

    private OnPreparedListener onPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            if(isListenerAttached()){
                playbackListener.onSongChanged(getCurrentSong());
            }
        }
    };

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
//        setOnPreparedListener(onPreparedListener);
    }

    public LocalPlayer(Context context, List<Song> playlist){
        this.context = context;
        active = false;
        currentQueuePosition = -1;
        queue = playlist;
        setOnCompletionListener(onCompletionListener);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setShuffle(sharedPreferences.getBoolean("shuffle", false));
        setRepeat(sharedPreferences.getBoolean("repeat", false));
//        setOnPreparedListener(onPreparedListener);
    }

    public static void setQueue(List<Song> newQueue){
        queue = newQueue;
    }

    public static List<Song> getQueue(){
        return queue;
    }

    public static Song getCurrentSong(){
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

//            long start = System.currentTimeMillis();
//            start();
//            long end = System.currentTimeMillis();
//            Log.d(App.TAG, "start() takes "+(end-start)+" ms");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(App.TAG, "LocalPlayer: play("+position+")");
    }

    public static boolean hasCurrentSong() {
        return active;
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "LocalPlayer: nextSong");

        played.add(currentQueuePosition);

        int listSize = queue.size();

        if(shuffle){
            currentQueuePosition = generator.nextInt(listSize-1);
        }
        if(++currentQueuePosition < listSize){
            play(currentQueuePosition);
        }else{
            --currentQueuePosition;
            Toast t = Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT);
            t.show();
        }

    }

    @Override
    public void prevSong(){
        Log.d(App.TAG, "LocalPlayer: prevSong");

        if(isShuffle()){
            if(!played.isEmpty()){
                currentQueuePosition = played.pollLast();
                play(currentQueuePosition);
            }else{
                Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
                t.show();
            }
        }else{
            if(--currentQueuePosition >= 0){
                play(currentQueuePosition);
                Log.d(App.TAG, "LocalPlayer: nextSong");
            }else{
                ++currentQueuePosition;
                Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
                t.show();
            }
        }

    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if(StreamServer.hasClients()){
            new ClientsManager(this).pause();
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
    }

    @Override
    public void togglePause(){
        if (isPlaying()){
            pause();
        }else{
            start();
        }
    }

    private void fillIndexes(int n){
        for(int i=0;i<n;i++){
            indexes.add(i);
        }
    }

    private void addIndexes(int n){
        int x = indexes.size();
        for(int i=x;i<x+n;i++){
            indexes.add(i);
        }
    }

    public static int getCurrentQueuePosition() {
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