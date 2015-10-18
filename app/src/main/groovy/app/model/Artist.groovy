package app.model
import android.database.Cursor
import app.data_managers.AlbumsManager
import app.data_managers.CursorInitializable
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import rx.Observable

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.ArtistColumns.*

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass})
class Artist implements CursorInitializable {
    long id
    int numberOfAlbums
    int numberOfSongs
    String name

    Observable<Album> getAlbums() {
        AlbumsManager.loadAllAlbums(this)
    }

    Artist() {}

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        id = cursor.getInt indices[_ID]
        name = cursor.getString indices[ARTIST]
        numberOfAlbums = cursor.getInt indices[NUMBER_OF_ALBUMS]
        numberOfSongs = cursor.getInt indices[NUMBER_OF_TRACKS]
    }
}
