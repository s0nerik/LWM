package app.model;

import android.database.Cursor;

import app.helper.db.ArtistsCursorGetter
import groovy.transform.CompileStatic;

import java.util.ArrayList;
import java.util.List;

@CompileStatic
public class ArtistWrapperList {

    List<ArtistWrapper> artistWrappers = new ArrayList<>();

    public ArtistWrapperList(Cursor cursor) {
        if(cursor.moveToFirst()) {
            artistWrappers << buildArtistWrapperFromCursor(cursor)
            while (cursor.moveToNext()) {
                artistWrappers << buildArtistWrapperFromCursor(cursor)
            }
        }
        cursor.close();
    }

    private static ArtistWrapper buildArtistWrapperFromCursor(Cursor cursor) {
        new ArtistWrapper(artist:
                Artist.builder()
                .id(cursor.getInt(ArtistsCursorGetter._ID))
                .name(cursor.getString(ArtistsCursorGetter.ARTIST))
                .numberOfAlbums(cursor.getInt(ArtistsCursorGetter.NUMBER_OF_ALBUMS))
                .numberOfSongs(cursor.getInt(ArtistsCursorGetter.NUMBER_OF_TRACKS))
                .build()
        )
    }
}