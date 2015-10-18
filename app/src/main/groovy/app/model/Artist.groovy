package app.model

import android.database.Cursor
import app.helper.db.AlbumsCursorGetter
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.ArtistColumns.*

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass})
class Artist {
    long id
    int numberOfAlbums
    int numberOfSongs
    String name

    public List<Album> getAlbums() {
        AlbumsCursorGetter albumsCursorGetter = new AlbumsCursorGetter()
        Cursor cursor = albumsCursorGetter.getAlbumsCursorByArtist(this)
        AlbumsList albumsList = new AlbumsList(cursor)
        return albumsList.getAlbums()
    }

    static Artist fromCursor(Cursor cursor, Map<String, Integer> indices) {
        Artist.builder()
                .id(cursor.getInt(indices[_ID]))
                .name(cursor.getString(indices[ARTIST]))
                .numberOfAlbums(cursor.getInt(indices[NUMBER_OF_ALBUMS]))
                .numberOfSongs(cursor.getInt(indices[NUMBER_OF_TRACKS]))
                .build()
    }

}
