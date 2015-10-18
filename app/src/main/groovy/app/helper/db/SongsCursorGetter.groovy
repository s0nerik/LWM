package app.helper.db

import android.net.Uri
import android.provider.MediaStore.Audio.Media
import android.support.annotation.NonNull
import app.model.Album
import groovy.transform.CompileStatic

@CompileStatic
final class SongsCursorGetter extends CursorGetter {

    Uri contentUri = Media.EXTERNAL_CONTENT_URI
    List<String> selection = [ "${Media.IS_MUSIC} != 0" as String ]

    List<String> projection = [
            Media._ID,
            Media.TITLE,
            Media.ARTIST,
            Media.ALBUM,
            Media.DURATION,
            Media.DATA,
            Media.DISPLAY_NAME,
            Media.SIZE,
            Media.ALBUM_ID,
            Media.ARTIST_ID,
            Media.TRACK
    ]

    SortOrder sortOrder = new SortOrder([Media.ARTIST, Media.ALBUM, Media.TRACK, Media.DISPLAY_NAME], Order.ASCENDING)

    private Album album

    //region Constructors
    SongsCursorGetter() {}

    SongsCursorGetter(@NonNull Album album) {
        this.album = album
        selection << ("$Media.ALBUM_ID = $album.id" as String)
    }

    SongsCursorGetter(@NonNull SortOrder sortOrder) {
        this.sortOrder = sortOrder
    }

    SongsCursorGetter(@NonNull Album album, @NonNull SortOrder sortOrder) {
        this(album)
        this.sortOrder = sortOrder
    }
    //endregion
}
