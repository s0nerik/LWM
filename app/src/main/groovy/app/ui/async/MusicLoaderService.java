package app.ui.async;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.joanzapata.android.asyncservice.api.annotation.AsyncService;
import app.helper.db.AlbumsCursorGetter;
import app.helper.db.ArtistsCursorGetter;
import app.helper.db.Order;
import app.helper.db.SongsCursorGetter;
import app.model.Album;
import app.model.AlbumsList;
import app.model.Artist;
import app.model.ArtistWrapper;
import app.model.ArtistWrapperList;
import app.model.Playlist;
import app.model.Song;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@AsyncService
public class MusicLoaderService {

    public SongsLoadedEvent loadAllSongs() {
        return new SongsLoadedEvent(Playlist.fromCursor(new SongsCursorGetter().getSongsCursor(Order.ASCENDING)));
    }

    public ArtistsLoadedEvent loadAllArtists() {
        return new ArtistsLoadedEvent(new ArtistWrapperList(new ArtistsCursorGetter().getArtistsCursor()).getArtistWrappers());
    }

    public AlbumsLoadedEvent loadAllAlbums(@Nullable Artist artist) {
        AlbumsCursorGetter cursorGetter = new AlbumsCursorGetter();
        Cursor albums;

        if (artist == null) {
            albums = cursorGetter.getAlbumsCursor();
        } else {
            albums = cursorGetter.getAlbumsCursorByArtist(artist);
        }

        return new AlbumsLoadedEvent(artist, new AlbumsList(albums).getAlbums());
    }

    @Data
    @RequiredArgsConstructor
    public static class SongsLoadedEvent {
        private final List<Song> songs;
    }

    @Data
    @RequiredArgsConstructor
    public static class ArtistsLoadedEvent {
        private final List<ArtistWrapper> artists;
    }

    @Data
    @RequiredArgsConstructor
    public static class AlbumsLoadedEvent {
        private final Artist artist;
        private final List<Album> albums;
    }

}
