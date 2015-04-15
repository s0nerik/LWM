package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.db.SongsCursorGetter;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    public static List<Song> fromCursor(Cursor cursor){
        List<Song> songs = new ArrayList<>();
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
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
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return songs;
    }

//    private List<Song> songs = new ArrayList<>();
//
//    public Playlist(){}
//
//    public Playlist(Cursor cursor){
//        if(cursor.moveToFirst()) {
//            do{
//                songs.add(new Song(
//                        cursor.getLong(SongsCursorGetter._ID),
//                        cursor.getLong(SongsCursorGetter.ARTIST_ID),
//                        cursor.getLong(SongsCursorGetter.ALBUM_ID),
//                        cursor.getString(SongsCursorGetter.TITLE),
//                        cursor.getString(SongsCursorGetter.ARTIST),
//                        cursor.getString(SongsCursorGetter.ALBUM),
//                        cursor.getString(SongsCursorGetter.DATA),
//                        cursor.getInt(SongsCursorGetter.DURATION)
//                ));
//            }while(cursor.moveToNext());
//        }
//        cursor.close();
//    }
//
//    public Song getSong(int pos) {
//        return songs.get(pos);
//    }
//
//    public List<Song> getSongs() {
//        return songs;
//    }
//
//    public void append(Playlist playlist){
//        songs.addAll(playlist.getSongs());
//    }
//
//    public <T extends Collection<Song>> void addSongs(T songsToAdd){
//        songs.addAll(songsToAdd);
//    }
//
//    public int size(){
//        return songs.size();
//    }
//
//    public boolean isEmpty(){
//        return songs.isEmpty();
//    }

}