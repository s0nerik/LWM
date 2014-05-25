package com.lwm.app.model;

import android.database.Cursor;

import com.lwm.app.helper.ArtistsCursorGetter;

import java.util.ArrayList;
import java.util.List;

public class ArtistsList {

    private List<Artist> artists = new ArrayList<>();

    public ArtistsList(Cursor cursor) {
        if(cursor.moveToFirst()) {
            int id = ArtistsCursorGetter._ID;
            int artist = ArtistsCursorGetter.ARTIST;
            int numberOfAlbums = ArtistsCursorGetter.NUMBER_OF_ALBUMS;
            int numberOfTracks = ArtistsCursorGetter.NUMBER_OF_TRACKS;

            do {
                artists.add(new Artist(
                        cursor.getInt(id),
                        cursor.getString(artist),
                        cursor.getInt(numberOfAlbums),
                        cursor.getInt(numberOfTracks)
                ));
            } while (cursor.moveToNext());

//            cursor.close();
        }
        cursor.close();
    }

    public List<Artist> getArtists() {
        return artists;
    }
}