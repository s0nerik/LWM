package app.model

import android.database.Cursor
import app.helper.db.AlbumsCursorGetter
import com.arasthel.swissknife.annotations.Parcelable
import groovy.transform.builder.Builder;

@Builder
@Parcelable
public final class Artist {
    long id;
    int numberOfAlbums;
    int numberOfSongs;
    String name;

    public List<Album> getAlbums() {
        AlbumsCursorGetter albumsCursorGetter = new AlbumsCursorGetter();
        Cursor cursor = albumsCursorGetter.getAlbumsCursorByArtist(this);
        AlbumsList albumsList = new AlbumsList(cursor);
        return albumsList.getAlbums();
    }
}
