package app.model

import android.content.Context
import app.Daggered
import app.data_managers.AlbumsManager
import app.data_managers.ArtistsManager
import app.data_managers.SongsManager
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.collections4.MultiMap
import org.apache.commons.collections4.map.MultiValueMap
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

@CompileStatic
class MusicCollection extends Daggered implements Serializable {
    MultiMap<Artist, Album> artistAlbums = new MultiValueMap<>()
    MultiMap<Artist, Song> artistSongs = new MultiValueMap<>()
    MultiMap<Album, Song> albumSongs = new MultiValueMap<>()

    MultiMap<Album, Artist> albumArtists = new MultiValueMap<>()
    MultiMap<Song, Artist> songArtists = new MultiValueMap<>()
    MultiMap<Song, Album> songAlbums = new MultiValueMap<>()

    List<Song> songs = new ArrayList<>()
    List<Album> albums = new ArrayList<>()
    List<Artist> artists = new ArrayList<>()

    @Inject
    @PackageScope
    transient Context context

    void addSong(Song song, Artist artist, Album album) {
        if (album) {
            songAlbums[song] = album
            albumSongs[album] = song

            if (artist) albumArtists[album] = artist

            albums << album
        }

        if (artist) {
            songArtists[song] = artist
            artistSongs[artist] = song

            if (album) artistAlbums[artist] = album

            artists << artist
        }

        songs << song
    }

    Observable<Song> initFromFile() {
        Observable.create({ Subscriber<Song> subscriber ->
            clear()

            try {
                def stream = context.openFileInput "collection.lwm"
                new ObjectInputStream(stream).withObjectInputStream {
                    init(it.readObject() as MusicCollection)
                }

                songs.each { subscriber.onNext it }
                subscriber.onCompleted()
            } catch (e) {
                subscriber.onError(e)
            }
        } as Observable.OnSubscribe<Song>)
    }

    Observable<Song> initFromMediaStore() {
        Observable.create({ Subscriber<Song> subscriber ->
            clear()

            def artistsList = ArtistsManager.loadAllArtists().toList().toBlocking().single()
            def songsList = SongsManager.loadAllSongs().toList().toBlocking().single()
            def albumsList = AlbumsManager.loadAllAlbums().toList().toBlocking().single()

            songsList.each { Song song ->
                addSong song, artistsList.find { it.id == song.artistId },
                        albumsList.find { it.id == song.albumId }
            }

            def stream = context.openFileOutput("collection.lwm", Context.MODE_PRIVATE)

            new ObjectOutputStream(stream).withObjectOutputStream {
                it.writeObject(this as MusicCollection)
            }

            songs.each { subscriber.onNext it }
            subscriber.onCompleted()

        } as Observable.OnSubscribe<Song>)
    }

    private void clear() {
        artistAlbums.clear()
        artistSongs.clear()
        albumSongs.clear()
        albumArtists.clear()
        songArtists.clear()
        songAlbums.clear()
        songs.clear()
        albums.clear()
        artists.clear()
    }

    private void init(MusicCollection other) {
        artistAlbums = other.artistAlbums
        artistSongs = other.artistSongs
        albumSongs = other.albumSongs
        albumArtists = other.albumArtists
        songArtists = other.songArtists
        songAlbums = other.songAlbums
        songs = other.songs
        albums = other.albums
        artists = other.artists
    }
}