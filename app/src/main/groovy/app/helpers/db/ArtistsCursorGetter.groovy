package app.helpers.db
import android.net.Uri
import groovy.transform.CompileStatic

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS
import static android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST
import static android.provider.MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
import static android.provider.MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

@CompileStatic
final class ArtistsCursorGetter extends CursorGetter {

    Uri contentUri = EXTERNAL_CONTENT_URI
    List<String> projection = [
            _ID,
            ARTIST,
            NUMBER_OF_ALBUMS,
            NUMBER_OF_TRACKS
    ]
    List<String> selection = []
    SortOrder sortOrder = new StringSortOrder(DEFAULT_SORT_ORDER)

    ArtistsCursorGetter() {
    }

    ArtistsCursorGetter(long id) {
        selection << ("$_ID = $id" as String)
    }
}