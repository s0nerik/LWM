package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.db.AlbumsCursorGetter;

import java.util.List;

public class Artist {
    private long id;
    private String name;
    private int numberOfAlbums;
    private int numberOfSongs;

    public Artist(long id, String name, int numberOfAlbums, int numberOfSongs) {
        this.id = id;
        this.name = name;
        this.numberOfAlbums = numberOfAlbums;
        this.numberOfSongs = numberOfSongs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfAlbums() {
        return numberOfAlbums;
    }

    public void setNumberOfAlbums(int numberOfAlbums) {
        this.numberOfAlbums = numberOfAlbums;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public List<Album> getAlbums() {
        AlbumsCursorGetter albumsCursorGetter = new AlbumsCursorGetter();
        Cursor cursor = albumsCursorGetter.getAlbumsCursorByArtist(this);
        AlbumsList albumsList = new AlbumsList(cursor);
        return albumsList.getAlbums();
    }
}
