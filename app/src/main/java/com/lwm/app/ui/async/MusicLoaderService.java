package com.lwm.app.ui.async;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.joanzapata.android.asyncservice.api.annotation.AsyncService;
import com.lwm.app.helper.db.AlbumsCursorGetter;
import com.lwm.app.helper.db.ArtistsCursorGetter;
import com.lwm.app.helper.db.SongsCursorGetter;
import com.lwm.app.model.Album;
import com.lwm.app.model.AlbumsList;
import com.lwm.app.model.Artist;
import com.lwm.app.model.ArtistWrapper;
import com.lwm.app.model.ArtistWrapperList;
import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@AsyncService
public class MusicLoaderService {

    public SongsLoadedEvent loadAllSongs() {
        return new SongsLoadedEvent(Playlist.fromCursor(new SongsCursorGetter().getSongsCursor()));
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
