package app.helper.db

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore

import app.Daggered
import app.model.Album
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

import android.provider.MediaStore.Audio.Media

@CompileStatic
final class SongsCursorGetter extends Daggered {

    @Inject
    @PackageScope
    ContentResolver contentResolver

    private String selection = "${Media.IS_MUSIC} != 0"
    private String[] projection = [
            Media._ID,
            Media.TITLE,
            Media.ARTIST,
            Media.ALBUM,
            Media.DURATION,
            Media.DATA,
            Media.DISPLAY_NAME,
            Media.SIZE,
            Media.ALBUM_ID,
            Media.ARTIST_ID
    ]

    static final int _ID          = 0
    static final int TITLE        = 1
    static final int ARTIST       = 2
    static final int ALBUM        = 3
    static final int DURATION     = 4
    static final int DATA         = 5
    static final int DISPLAY_NAME = 6
    static final int SIZE         = 7
    static final int ALBUM_ID     = 8
    static final int ARTIST_ID    = 9

    Observable<Cursor> getSongsCursor(Order order, Album album) {
        def selectionArgs = null
        String selection = this.selection
        if (album?.id > -1) {
            selection = "${this.selection} AND $MediaStore.Audio.AudioColumns.ALBUM_ID = ?"
            selectionArgs = album.id as String[]
        }

        String orderString = ""
        switch (order) {
            case Order.ASCENDING:
                orderString = "ASC"
                break
            case Order.DESCENDING:
                orderString = "DESC"
                break
            case Order.RANDOM:
                orderString = "random()"
                break
        }

        if (order != Order.RANDOM) {
            orderString = "$Media.ARTIST $orderString, $Media.ALBUM_ID $orderString, $Media.TRACK $orderString, $Media.DISPLAY_NAME $orderString"
        }

        Observable.create({ Subscriber<Cursor> subscriber ->
            subscriber.onNext contentResolver.query(
                    Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    (String[]) selectionArgs,
                    orderString
            )

            subscriber.onCompleted()
        } as Observable.OnSubscribe<Cursor>)
    }

    Observable<Cursor> getSongsCursor(Order order){
        return getSongsCursor(order, null)
    }
}
