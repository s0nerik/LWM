package app.helper.db
import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore.Audio.Media
import app.Daggered
import app.model.Album
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

@CompileStatic
final class SongsCursorGetter extends Daggered {

    @Inject
    @PackageScope
    ContentResolver contentResolver

    private String selection = "${Media.IS_MUSIC} != ?"
    private List<String> selectionArgs = ["0"]

    @TupleConstructor
    static enum Column {
        ID              (Media._ID),
        TITLE           (Media.TITLE),
        ARTIST          (Media.ARTIST),
        ALBUM           (Media.ALBUM),
        DURATION        (Media.DURATION),
        DATA            (Media.DATA),
        DISPLAY_NAME    (Media.DISPLAY_NAME),
        SIZE            (Media.SIZE),
        ALBUM_ID        (Media.ALBUM_ID),
        ARTIST_ID       (Media.ARTIST_ID),
        TRACK           (Media.TRACK)

        final String name

        static List<String> names() {
            values().collect { Column it -> it.name }
        }

        static Map<Column, Integer> indicesInCursor(Cursor c) {
            def indices = [:]

            values().each { Column it ->
                indices[it] = c.getColumnIndex(it.name)
            }

            return indices
        }
    }

    Observable<Cursor> getSongsCursor(Order order, Album album) {
        def selectionArgs = this.selectionArgs.clone() as List<String>
        String selection = this.selection
        if (album?.id > -1) {
            selection = "${selection} AND $Media.ALBUM_ID = ?"
            selectionArgs << (album.id as String)
        }

        def sortOrders = [
                (Order.ASCENDING): "ASC",
                (Order.DESCENDING): "DESC",
                (Order.RANDOM): "random()"
        ]

        String sortOrder = sortOrders[order]
        if (order != Order.RANDOM) {
            sortOrder = """\
$Column.ARTIST.name ${sortOrders[order]},
$Column.ALBUM.name ${sortOrders[order]},
$Column.TRACK.name ${sortOrders[order]},
$Column.DISPLAY_NAME.name ${sortOrders[order]}"""
        }

        Observable.create({ Subscriber<Cursor> subscriber ->
            subscriber.onNext contentResolver.query(
                    Media.EXTERNAL_CONTENT_URI,
                    Column.names() as String[],
                    selection,
                    selectionArgs as String[],
                    sortOrder
            )

            subscriber.onCompleted()
        } as Observable.OnSubscribe<Cursor>)
    }

    Observable<Cursor> getSongsCursor(Order order){
        return getSongsCursor(order, null)
    }
}
