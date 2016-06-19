package app.models
import android.database.Cursor
import app.Daggered
import app.helpers.db.cursor_constructor.CursorInitializable
import app.helpers.CollectionManager
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.builder.Builder

import javax.inject.Inject

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART
import static android.provider.MediaStore.Audio.AlbumColumns.ARTIST
import static android.provider.MediaStore.Audio.AlbumColumns.FIRST_YEAR
import static android.provider.MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST_ID

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass; artist; songs; collectionManager})
final class Album extends Daggered implements CursorInitializable, Serializable {

    @Inject
    @PackageScope
    transient CollectionManager collectionManager

    long id
    int year
    int songsCount
    String title
    String artistName
    String albumArtPath

    long artistId

    Artist getArtist() {
        collectionManager.getArtist this
    }

    List<Song> getSongs() {
        collectionManager.getSongs this
    }

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        id = cursor.getLong indices[_ID]
        title = cursor.getString indices[ALBUM]
        artistName = cursor.getString indices[ARTIST]
        year = cursor.getInt indices[FIRST_YEAR]
        albumArtPath = cursor.getString indices[ALBUM_ART]
        songsCount = cursor.getInt indices[NUMBER_OF_SONGS]

        artistId = cursor.getLong indices[ARTIST_ID]
    }

}