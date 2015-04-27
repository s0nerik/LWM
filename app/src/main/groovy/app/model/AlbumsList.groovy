package app.model
import android.database.Cursor
import app.helper.db.AlbumsCursorGetter
import groovy.transform.CompileStatic

@CompileStatic
public class AlbumsList {

    List<Album> albums = new ArrayList<>();

    public AlbumsList(Cursor cursor) {
        if(cursor.moveToFirst()) {
            albums << buildAlbumFromCursor(cursor)
            while (cursor.moveToNext()) {
                albums << buildAlbumFromCursor(cursor)
            }
        }
        cursor.close();
    }

    private static Album buildAlbumFromCursor(Cursor cursor) {
        Album.builder()
                .id(cursor.getInt(AlbumsCursorGetter._ID))
                .title(cursor.getString(AlbumsCursorGetter.ALBUM))
                .artist(cursor.getString(AlbumsCursorGetter.ARTIST))
                .year(cursor.getInt(AlbumsCursorGetter.FIRST_YEAR))
                .albumArtPath(cursor.getString(AlbumsCursorGetter.ALBUM_ART))
                .songsCount(cursor.getInt(AlbumsCursorGetter.NUMBER_OF_SONGS))
                .build()
    }
}
