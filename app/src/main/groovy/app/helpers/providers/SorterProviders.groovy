package app.helpers.providers

import app.R
import app.adapters.albums.AlbumItem
import app.adapters.artists.ArtistItem
import app.adapters.songs.SongItem
import groovy.transform.CompileStatic

@CompileStatic
class SorterProviders {
    static final Map<Integer, Closure> SONGS = [
            (R.id.songs_sort_title) : { SongItem it -> it.song.title },
            (R.id.songs_sort_artist): { SongItem it -> it.song.artistName },
            (R.id.songs_sort_album) : { SongItem it -> it.song.albumName },
            (R.id.songs_sort_year)  : { SongItem it -> it.song?.album?.year },
    ]

    static final Map<Integer, Closure> ALBUMS = [
            (R.id.albums_sort_title)    : { AlbumItem it -> it.album.title },
            (R.id.albums_sort_artist)   : { AlbumItem it -> it.album.artistName },
            (R.id.albums_sort_year)     : { AlbumItem it -> it.album.year },
    ]

    static final Map<Integer, Closure> ARTISTS = [
            (R.id.artists_sort_name)            : { ArtistItem it -> it.artist.name },
            (R.id.artists_sort_recent_album)    : { ArtistItem it -> it.artist.albums.sort { it.year }.last().year },
    ]
}