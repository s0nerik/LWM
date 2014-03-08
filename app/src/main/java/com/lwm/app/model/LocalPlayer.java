package com.lwm.app.model;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.helper.SongsCursorGetter;

import java.io.IOException;

public class LocalPlayer extends BasePlayer {

    private int currentListPosition;
    private int listSize;
    private Cursor playlist;
    private Context context;

    private Uri artworkUri = Uri.parse("content://media/external/audio/albumart");

    public LocalPlayer(Context context){
        this.context = context;
        initOnCompletionListener();
    }

    public LocalPlayer(Context context, Cursor playlist){
        this.context = context;
        initOnCompletionListener();
        setPlaylist(playlist);
        listSize = playlist.getCount();
    }

    private void initOnCompletionListener(){
        setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d("LWM", "LocalPlayer: onCompletion");

                nextSong();
            }
        });
    }

    public void setPlaylist(Cursor playlist){
        this.playlist = playlist;
        listSize = playlist.getCount();
    }

    public Cursor getPlaylist(){
        return playlist;
    }

    public String getCurrentSource(){
        Log.d(App.TAG, "getCurrentSource: " + playlist.getString(SongsCursorGetter.DATA));
        return playlist.getString(SongsCursorGetter.DATA);
    }

    public void play(int position){
        playlist.moveToPosition(position);

        currentListPosition = position;

        reset();
        try {
            setDataSource(getCurrentSource());
            prepare();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(App.TAG, "LocalPlayer: play("+position+")");
    }

    @Override
    public void nextSong() {
        if(++currentListPosition < listSize){
            play(currentListPosition);
            Log.d(App.TAG, "LocalPlayer: nextSong");

            Intent intent = new Intent(SONG_CHANGED);
            intent.putExtra(PLAYLIST_POSITION, currentListPosition);

            context.sendBroadcast(intent);
        }else{
            --currentListPosition;
            Toast t = Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    @Override
    public void prevSong(){
        Log.d(App.TAG, "LocalPlayer: prevSong");

        if(--currentListPosition >= 0){
            play(currentListPosition);
            Intent intent = new Intent(SONG_CHANGED);
            intent.putExtra(PLAYLIST_POSITION, currentListPosition);

            context.sendBroadcast(intent);
        }else{
            ++currentListPosition;
            Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
            t.show();
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

    public String getCurrentArtist(){
        return playlist.getString(SongsCursorGetter.ARTIST);
    }

    public String getCurrentTitle(){
        return playlist.getString(SongsCursorGetter.TITLE);
    }

    public String getCurrentAlbum(){
        return playlist.getString(SongsCursorGetter.ALBUM);
    }

    public int getCurrentAlbumId(){
        return playlist.getInt(SongsCursorGetter.ALBUM_ID);
    }

    public int getCurrentDuration(){
        return playlist.getInt(SongsCursorGetter.DURATION);
    }

    public Uri getCurrentAlbumArtUri(){
        return ContentUris.withAppendedId(artworkUri, getCurrentAlbumId());
    }

    public int getCurrentListPosition() {
        return currentListPosition;
    }
}