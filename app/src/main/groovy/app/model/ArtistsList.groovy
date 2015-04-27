package app.model
import android.database.Cursor
import app.helper.db.ArtistsCursorGetter
import groovy.transform.CompileStatic

@CompileStatic
public class ArtistsList {

    List<Artist> artists = new ArrayList<>();

    ArtistsList(Cursor cursor) {
        if(cursor.moveToFirst()) {
            artists << buildArtistFromCursor(cursor)
            while (cursor.moveToNext()) {
                artists << buildArtistFromCursor(cursor)
            }
        }
        cursor.close();
    }

    private static Artist buildArtistFromCursor(Cursor cursor) {
        Artist.builder()
                .id(cursor.getInt(ArtistsCursorGetter._ID))
                .name(cursor.getString(ArtistsCursorGetter.ARTIST))
                .numberOfAlbums(cursor.getInt(ArtistsCursorGetter.NUMBER_OF_ALBUMS))
                .numberOfSongs(cursor.getInt(ArtistsCursorGetter.NUMBER_OF_TRACKS))
                .build()
    }
}