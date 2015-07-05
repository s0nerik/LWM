package app.data_managers

import android.support.annotation.Nullable
import android.support.v4.util.Pair
import app.helper.db.AlbumsCursorGetter
import app.model.Album
import app.model.AlbumsList
import app.model.Artist
import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.AsyncSubject

@CompileStatic
public class AlbumsManager {

    private Map<Artist, AsyncSubject<Pair<Artist, List<Album>>>> albumSubjects = new HashMap<>()

    public Observable<Pair<Artist, List<Album>>> loadAllAlbums(@Nullable Artist artist) {
        if (!albumSubjects[artist]) {
            albumSubjects[artist] = AsyncSubject.create();

            AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter()

            Observable.just(new Pair<Artist, List<Album>>(artist,
                    new AlbumsList(artist ? cursorGetter.getAlbumsCursorByArtist(artist) : cursorGetter.getAlbumsCursor()).getAlbums()
            )).subscribe(albumSubjects[artist])
        }
        return albumSubjects[artist]
    }

//    public AlbumsLoadedEvent loadAllAlbums(@Nullable Artist artist) {
//        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter();
//        Cursor albums;
//
//        if (artist == null) {
//            albums = cursorGetter.getAlbumsCursor();
//        } else {
//            albums = cursorGetter.getAlbumsCursorByArtist(artist);
//        }
//
//        return new AlbumsLoadedEvent(artist: artist, albums: new AlbumsList(albums).getAlbums());
//    }
//
//    public static class AlbumsLoadedEvent {
//        Artist artist;
//        List<Album> albums;
//    }

}
