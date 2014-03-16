package com.lwm.app.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;

import java.io.IOException;

public class LocalPlayer extends BasePlayer {

    private int currentListPosition = -1;
    private static int listSize;
    private static Playlist playlist;
    private Context context;

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
                nextSong();
            }
        }
    };

    public LocalPlayer(Context context){
        this.context = context;
        setOnCompletionListener(onCompletionListener);
        setShuffle(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("shuffle", false));
//        setOnPreparedListener(onPreparedListener);
    }

    public LocalPlayer(Context context, Playlist playlist){
        this.context = context;
        setPlaylist(playlist);
        setOnCompletionListener(onCompletionListener);
        setShuffle(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("shuffle", false));
//        setOnPreparedListener(onPreparedListener);
    }

    public static void setPlaylist(Playlist newPlaylist){
        playlist = newPlaylist;
        listSize = playlist.size();
    }

    public static Playlist getPlaylist(){
        return playlist;
    }

    public Song getCurrentSong(){
        Log.d(App.TAG, "getCurrentSong");

//        assert playlist != null : "playlist == null";
        if(playlist != null){
            return playlist.getSong(currentListPosition);
        }else{
            return null;
        }
    }

    public void play(int position){
        currentListPosition = position;

        reset();
        try {
            assert playlist != null : "playlist == null";
            setDataSource(playlist.getSong(position).getSource());
            prepare();
            start();
            if(isListenerAttached()){
                playbackListener.onSongChanged(getCurrentSong());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setActive();

        Log.d(App.TAG, "LocalPlayer: play("+position+")");
    }

    @Override
    public void nextSong() {
        Log.d(App.TAG, "LocalPlayer: nextSong");

        played.add(currentListPosition);

        if(shuffle){
            currentListPosition = generator.nextInt(listSize-1);
        }
        if(++currentListPosition < listSize){
            play(currentListPosition);
        }else{
            --currentListPosition;
            Toast t = Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    @Override
    public void prevSong(){
        Log.d(App.TAG, "LocalPlayer: prevSong");

        if(isShuffle()){
            if(!played.isEmpty()){
                currentListPosition = played.pollLast();
                play(currentListPosition);
            }else{
                Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
                t.show();
            }
        }else{
            if(--currentListPosition >= 0){
                play(currentListPosition);
                Log.d(App.TAG, "LocalPlayer: nextSong");
            }else{
                ++currentListPosition;
                Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
                t.show();
            }
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

    public int getCurrentListPosition() {
        return currentListPosition;
    }
}