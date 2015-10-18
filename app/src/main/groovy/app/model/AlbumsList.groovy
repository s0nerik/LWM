package app.model
import android.database.Cursor
import app.helper.db.AlbumsCursorGetter
import groovy.transform.CompileStatic

@CompileStatic
class AlbumsList {

    List<Album> albums = []

    AlbumsList(Cursor cursor) {
        if(cursor.moveToFirst()) {
            albums << buildAlbumFromCursor(cursor)
            while (cursor.moveToNext()) {
                albums << buildAlbumFromCursor(cursor)
            }
        }
        cursor.close();
    }

}