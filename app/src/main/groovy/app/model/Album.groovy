package app.model
import android.database.Cursor
import app.data_managers.CursorInitializable
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART
import static android.provider.MediaStore.Audio.AlbumColumns.ARTIST
import static android.provider.MediaStore.Audio.AlbumColumns.FIRST_YEAR
import static android.provider.MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass})
final class Album implements CursorInitializable {
    int id
    int year
    int songsCount
    String title
    String artist
    String albumArtPath

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        id = cursor.getInt indices[_ID]
        title = cursor.getString indices[ALBUM]
        artist = cursor.getString indices[ARTIST]
        year = cursor.getInt indices[FIRST_YEAR]
        albumArtPath = cursor.getString indices[ALBUM_ART]
        songsCount = cursor.getInt indices[NUMBER_OF_SONGS]
    }

}