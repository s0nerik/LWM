package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.SongsCursorGetter;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private List<Song> songs = new ArrayList<>();

    public Playlist(Cursor cursor){
        if(cursor.moveToFirst()) {
            do{
                songs.add(new Song(
                        cursor.getLong(SongsCursorGetter._ID),
                        cursor.getLong(SongsCursorGetter.ARTIST_ID),
                        cursor.getLong(SongsCursorGetter.ALBUM_ID),
                        cursor.getString(SongsCursorGetter.TITLE),
                        cursor.getString(SongsCursorGetter.ARTIST),
                        cursor.getString(SongsCursorGetter.ALBUM),
                        cursor.getString(SongsCursorGetter.DATA),
                        cursor.getInt(SongsCursorGetter.DURATION)
                ));
            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    public Song getSong(int pos) {
        return songs.get(pos);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void append(Playlist playlist){
        songs.addAll(playlist.getSongs());
    }

    public int size(){
        return songs.size();
    }

}