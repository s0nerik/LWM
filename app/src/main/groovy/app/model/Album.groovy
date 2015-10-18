package app.model
import android.database.Cursor
import app.data_managers.ArtistsManager
import app.data_managers.CursorInitializable
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import rx.Observable

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AlbumColumns.*
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST_ID

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass})
final class Album implements CursorInitializable {
    int id
    int year
    int songsCount
    String title
    String artistName
    String albumArtPath

    long artistId

    Album() {}

    Observable<Artist> getArtist() {
        ArtistsManager.loadArtistById(artistId)
    }

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        id = cursor.getInt indices[_ID]
        title = cursor.getString indices[ALBUM]
        artistName = cursor.getString indices[ARTIST]
        year = cursor.getInt indices[FIRST_YEAR]
        albumArtPath = cursor.getString indices[ALBUM_ART]
        songsCount = cursor.getInt indices[NUMBER_OF_SONGS]

        artistId = cursor.getLong indices[ARTIST_ID]
    }

}