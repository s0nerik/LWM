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
import static android.provider.MediaStore.Audio.ArtistColumns.ARTIST
import static android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS
import static android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass; albums; collectionManager})
class Artist extends Daggered implements CursorInitializable, Serializable {

    @Inject
    @PackageScope
    transient CollectionManager collectionManager

    long id
    int numberOfAlbums
    int numberOfSongs
    String name

    List<Album> getAlbums() {
        collectionManager.getAlbums this
    }

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        id = cursor.getInt indices[_ID]
        name = cursor.getString indices[ARTIST]
        numberOfAlbums = cursor.getInt indices[NUMBER_OF_ALBUMS]
        numberOfSongs = cursor.getInt indices[NUMBER_OF_TRACKS]
    }
}
