package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.SongsCursorGetter;

import java.util.ArrayList;
import java.util.Collections;

public class CursorPlaylist implements Playlist {

    private int currentPosition = 0;
    private int currentShuffledPosition = 0;
    private boolean shuffle = false;
    private Cursor cursor;

    private ArrayList<Integer> normalPlaylist = new ArrayList<>();
    private ArrayList<Integer> shuffledPlaylist;
    private ArrayList<Integer> playlist;

    public CursorPlaylist(Cursor cursor){
        this.cursor = cursor;
        cursor.moveToFirst();
        for(int i=0; i < cursor.getCount(); i++){
            normalPlaylist.add(i);
        }
        shuffledPlaylist = normalPlaylist;
        playlist = normalPlaylist;
    }

    @Override
    public String getSource() {
        return cursor.getString(SongsCursorGetter.DATA);
    }

    @Override
    public void next() {
        if(!shuffle){
            cursor.moveToPosition(playlist.get(++currentPosition));
        }else{
            cursor.moveToPosition(shuffledPlaylist.get(++currentShuffledPosition));
        }
    }

    @Override
    public void prev() {
        if(!shuffle){
            cursor.moveToPosition(playlist.get(--currentPosition));
        }else{
            cursor.moveToPosition(shuffledPlaylist.get(--currentShuffledPosition));
        }
    }

    @Override
    public void moveTo(int pos) {
        if(!shuffle){
            currentPosition = pos;
            cursor.moveToPosition(playlist.get(pos));
        }else{
            currentShuffledPosition = pos;
            cursor.moveToPosition(shuffledPlaylist.get(pos));
        }
    }

    @Override
    public void setShuffle(boolean on) {
        shuffle = on;
        if(on){
            int first = playlist.get(currentPosition);
            shuffledPlaylist.remove(Integer.valueOf(first));
            Collections.shuffle(shuffledPlaylist);
            shuffledPlaylist.set(0, first);
            currentShuffledPosition = 0;
        }else{
            currentPosition = shuffledPlaylist.get(currentShuffledPosition);
        }
    }

    @Override
    public String getCurrentArtist(){
        return cursor.getString(SongsCursorGetter.ARTIST);
    }

    @Override
    public String getCurrentTitle(){
        return cursor.getString(SongsCursorGetter.TITLE);
    }

    @Override
    public String getCurrentAlbum(){
        return cursor.getString(SongsCursorGetter.ALBUM);
    }

    @Override
    public int getCurrentAlbumId(){
        return cursor.getInt(SongsCursorGetter.ALBUM_ID);
    }

    @Override
    public int getCurrentDuration(){
        return cursor.getInt(SongsCursorGetter.DURATION);
    }

    @Override
    public int getCurrentListPosition() {
        if(!shuffle){
            return currentPosition;
        }else{
            return currentShuffledPosition;
        }
    }

}