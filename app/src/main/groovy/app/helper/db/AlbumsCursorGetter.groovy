package app.helper.db
import android.net.Uri
import app.model.Artist
import groovy.transform.CompileStatic

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AlbumColumns.*
import static android.provider.MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
import static android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import static android.provider.MediaStore.Audio.AudioColumns.*

@CompileStatic
final class AlbumsCursorGetter extends CursorGetter {

    Uri contentUri = EXTERNAL_CONTENT_URI

    List<String> projection = [
            _ID,
            ALBUM,
            ALBUM_ART,
            ARTIST,
            FIRST_YEAR,
            NUMBER_OF_SONGS,
            ARTIST_ID,
    ]

    List<String> selection = []

    SortOrder sortOrder = new StringSortOrder(DEFAULT_SORT_ORDER)

    AlbumsCursorGetter() {}

    AlbumsCursorGetter(Artist artist) {
        selection << ("$ARTIST_ID = $artist.id" as String)
    }

    AlbumsCursorGetter(long id) {
        selection << ("$_ID = $id" as String)
    }
}
