package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.db.AlbumsCursorGetter;

import java.util.List;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class Artist {
    private long id;
    private String name;
    private int numberOfAlbums;
    private int numberOfSongs;

    public List<Album> getAlbums() {
        AlbumsCursorGetter albumsCursorGetter = new AlbumsCursorGetter();
        Cursor cursor = albumsCursorGetter.getAlbumsCursorByArtist(this);
        AlbumsList albumsList = new AlbumsList(cursor);
        return albumsList.getAlbums();
    }
}
