package app.helper.db;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import app.Daggered;
import app.model.Artist
import groovy.transform.CompileStatic;

import javax.inject.Inject;

@CompileStatic
public final class ArtistsCursorGetter extends Daggered {

    @Inject
    ContentResolver contentResolver;

    private final String[] projection = [
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    ];

    public static final int _ID              = 0;
    public static final int ARTIST           = 1;
    public static final int NUMBER_OF_ALBUMS = 2;
    public static final int NUMBER_OF_TRACKS = 3;

    public Cursor getArtistsCursor(){
        return contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        );
    }

    public Artist getArtistById(long id){
        String selection = MediaStore.Audio.Artists._ID + " = ?";
        String[] selectionArgs = [ String.valueOf(id) ];
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        Artist artist = null;
        if (cursor.moveToFirst()) {
            artist = Artist.builder()
                    .id(cursor.getInt(_ID))
                    .name(cursor.getString(ARTIST))
                    .numberOfAlbums(cursor.getInt(NUMBER_OF_ALBUMS))
                    .numberOfSongs(cursor.getInt(NUMBER_OF_TRACKS))
                    .build();
        }
        cursor.close();

        return artist;
    }

}