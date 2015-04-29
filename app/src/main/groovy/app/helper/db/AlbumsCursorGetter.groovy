package app.helper.db;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import app.Daggered;
import app.model.Album;
import app.model.Artist
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget;

import javax.inject.Inject;

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public final class AlbumsCursorGetter extends Daggered {

    private String artist;

    @Inject
    ContentResolver contentResolver;

    private final String[] projection = [
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.FIRST_YEAR,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
//            MediaStore.Audio.Albums.ALBUM_KEY
//            MediaStore.Audio.AudioColumns.ALBUM_ID
    ];

    private String selection = null;
    private String[] selectionArgs = null;

    public static final int _ID             = 0;
    public static final int ALBUM           = 1;
    public static final int ALBUM_ART       = 2;
    public static final int ARTIST          = 3;
    public static final int FIRST_YEAR      = 4;
    public static final int NUMBER_OF_SONGS = 5;
//    public static final int ALBUM_KEY    = 4;
//    public static final int ALBUM_ID     = 4;


    public AlbumsCursorGetter() {
        super();
    }

    public AlbumsCursorGetter(String artist) {
        super();
        this.artist = artist;
    }

    public Cursor getAlbumsCursor(){

        if(artist != null){
            selection = MediaStore.Audio.Artists.Albums.ARTIST + " = ?";
            selectionArgs = [artist];
        }

        return contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
        );

    }

    public Album getAlbumById(long id){
        String selection = MediaStore.Audio.Albums._ID + " = ?";
        String[] selectionArgs = [ String.valueOf(id) ];
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

        Album album = null;
        if (cursor.moveToFirst()) {
            album = Album.builder()
                    .id(cursor.getInt(_ID))
                    .title(cursor.getString(ALBUM))
                    .artist(cursor.getString(ARTIST))
                    .year(cursor.getInt(FIRST_YEAR))
                    .albumArtPath(cursor.getString(ALBUM_ART))
                    .songsCount(cursor.getInt(NUMBER_OF_SONGS))
                    .build();
        }
        cursor.close();

        return album;
    }

    public Cursor getAlbumsCursorByArtist(Artist artist){
        String selection = MediaStore.Audio.Albums.ARTIST + " = ?";
        String[] selectionArgs = [ artist.getName() ];

        return contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

}
