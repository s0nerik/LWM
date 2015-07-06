package app.model

import android.database.Cursor
import app.helper.db.AlbumsCursorGetter
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder;

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass})
public class Artist {
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
