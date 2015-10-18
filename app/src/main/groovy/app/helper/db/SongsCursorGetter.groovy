package app.helper.db
import android.database.Cursor
import android.provider.MediaStore.Audio.Media
import app.model.Album
import groovy.transform.CompileStatic

@CompileStatic
final class SongsCursorGetter extends CursorGetter {

    String defaultSelection = "${Media.IS_MUSIC} != ?"
    List<String> defaultSelectionArgs = ["0"]

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

    SortOrder defaultSortOrder = new SortOrder([Media.ARTIST, Media.ALBUM, Media.TRACK, Media.DISPLAY_NAME], Order.ASCENDING)

    private Album album
    private SortOrder sortOrder

    //region Constructors
    SongsCursorGetter() {}

    SongsCursorGetter(Album album) {
        this.album = album
    }

    SongsCursorGetter(SortOrder sortOrder) {
        this.sortOrder = sortOrder
    }

    SongsCursorGetter(Album album, SortOrder sortOrder) {
        this.album = album
        this.sortOrder = sortOrder
    }
    //endregion

    @Override
    Cursor getCursor() {
        def selection = defaultSelection
        def selectionArgs = defaultSelectionArgs
        def sortOrder = this.sortOrder ?: defaultSortOrder

        if (album) {
            selection = "${defaultSelection} AND $Media.ALBUM_ID = ?" as String
            selectionArgs = defaultSelectionArgs + (album.id as String)
        }

        getCursor sortOrder, selection, selectionArgs
    }
}
