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

public class MusicPlayer extends MediaPlayer {

    private int currentListPosition;
    private int listSize;
    private Cursor playlist;
    private Context context;

    public static final String SONG_CHANGED = "song_changed";
    public static final String PLAYBACK_STARTED = "playback_started";
    public static final String PLAYBACK_PAUSED = "playback_paused";
    public static final String PLAYLIST_POSITION = "playlist_position";
    public static final String CURRENT_POSITION = "current_position";
    public static final String SEEK_POSITION = "seek_position";
    public static final String ALBUM_ART_URI = "album_art_uri";

    private Uri artworkUri = Uri.parse("content://media/external/audio/albumart");

    public MusicPlayer(Context context){
        this.context = context;
        initOnCompletionListener();
    }

    public MusicPlayer(Context context, Cursor playlist){
        this.context = context;
        initOnCompletionListener();
        setPlaylist(playlist);
        listSize = playlist.getCount();
    }

    private void initOnCompletionListener(){
        setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d("LWM", "MusicPlayer: onCompletion");

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
        Log.d(App.TAG, "MusicPlayer: play("+position+")");
    }

    public void nextSong() {
        if(++currentListPosition < listSize){
            play(currentListPosition);
            Log.d(App.TAG, "MusicPlayer: nextSong");

            Intent intent = new Intent(SONG_CHANGED);
            intent.putExtra(PLAYLIST_POSITION, currentListPosition);

            Uri albumArtUri = getCurrentAlbumArtUri();

            if (albumArtUri != null) {
                intent.putExtra(ALBUM_ART_URI, albumArtUri.toString());
            }

            context.sendBroadcast(intent);
        }else{
            --currentListPosition;
            Toast t = Toast.makeText(context, "There's no next song", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    public void prevSong(){
        Log.d(App.TAG, "MusicPlayer: prevSong");

        if(--currentListPosition >= 0){
            play(currentListPosition);
            Intent intent = new Intent(SONG_CHANGED);
            intent.putExtra(PLAYLIST_POSITION, currentListPosition);

            Uri albumArtUri = getCurrentAlbumArtUri();

            if (albumArtUri != null) {
                intent.putExtra(ALBUM_ART_URI, albumArtUri.toString());
            }

            context.sendBroadcast(intent);
        }else{
            ++currentListPosition;
            Toast t = Toast.makeText(context, "There's no previous song", Toast.LENGTH_SHORT);
            t.show();
        }
    }

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

    public String getCurrentDurationInMinutes(){
        int seconds = getDuration()/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d",seconds);
    }

    public String getCurrentPositionInMinutes(){
        int seconds = getCurrentPosition()/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d",seconds);
    }

    public int getCurrentListPosition() {
        return currentListPosition;
    }
}