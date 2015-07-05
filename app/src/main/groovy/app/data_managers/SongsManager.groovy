package app.data_managers
import android.database.Cursor
import android.support.annotation.Nullable
import app.helper.db.AlbumsCursorGetter
import app.helper.db.ArtistsCursorGetter
import app.helper.db.Order
import app.helper.db.SongsCursorGetter
import app.model.*
import groovy.transform.CompileStatic
import rx.Observable
import rx.subjects.AsyncSubject

@CompileStatic
public class SongsManager {

    private AsyncSubject<List<Song>> songsSubject;

    public Observable<List<Song>> loadAllSongs() {
        if (songsSubject == null) {
            songsSubject = AsyncSubject.create();

            Observable.just(Playlist.fromCursor(new SongsCursorGetter().getSongsCursor(Order.ASCENDING)))
                    .subscribe(songsSubject)
        }
        return songsSubject;
    }

    public ArtistsLoadedEvent loadAllArtists() {
        return new ArtistsLoadedEvent(artists: new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers());
    }

    public AlbumsLoadedEvent loadAllAlbums(@Nullable Artist artist) {
        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter();
        Cursor albums;

        if (artist == null) {
            albums = cursorGetter.getAlbumsCursor();
        } else {
            albums = cursorGetter.getAlbumsCursorByArtist(artist);
        }

        return new AlbumsLoadedEvent(artist: artist, albums: new AlbumsList(albums).getAlbums());
    }

    public static class SongsLoadedEvent {
        List<Song> songs;
    }

    public static class ArtistsLoadedEvent {
        List<ArtistWrapper> artists;
    }

    public static class AlbumsLoadedEvent {
        Artist artist;
        List<Album> albums;
    }

}
