package app.helpers.db

import android.net.Uri
import app.App
import app.models.Artist
import groovy.transform.CompileStatic

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART
import static android.provider.MediaStore.Audio.AlbumColumns.FIRST_YEAR
import static android.provider.MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
import static android.provider.MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
import static android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST_ID

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

    AlbumsCursorGetter() {
        App.get().inject(this)
    }

    AlbumsCursorGetter(Artist artist) {
        this()
        selection << ("$ARTIST_ID = $artist.id" as String)
    }

    AlbumsCursorGetter(long id) {
        this()
        selection << ("$_ID = $id" as String)
    }
}
