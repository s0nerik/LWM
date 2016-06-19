package app.models
import android.content.Context
import app.Daggered
import app.helpers.GroovyLongSparseArray
import app.helpers.db.AlbumsCursorGetter
import app.helpers.db.ArtistsCursorGetter
import app.helpers.db.SongsCursorGetter
import app.helpers.db.cursor_constructor.CursorConstructor
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

@CompileStatic
class MusicCollection extends Daggered implements Serializable {
    GroovyLongSparseArray<Song> songs = new GroovyLongSparseArray<>()
    GroovyLongSparseArray<Album> albums = new GroovyLongSparseArray<>()
    GroovyLongSparseArray<Artist> artists = new GroovyLongSparseArray<>()

    @Inject
    @PackageScope
    transient Context context

    class FetchingMediaStoreStartedNotification {}
    class FetchingMediaStoreCompletedNotification {}
    class SongsLoadedNotification {}
    class ArtistsLoadedNotification {}
    class AlbumsLoadedNotification {}

    Observable<Object> init() {
        clear()
        initFromFile().onErrorResumeNext(initFromMediaStore())
    }

    private Observable<Object> initFromFile() {
        readAndInitCollectionFromFile()
                .doOnError { Debug.e it, "Collection initialization error." }
                .doOnCompleted { Debug.d "Collection initialized successfully." }
                .concatWith(Observable.just(this))
    }

    private Observable<Object> initFromMediaStore() {
        loadAllSongs().cast(Object)
                      .doOnCompleted { Debug.d "Songs loaded..." }
                      .concatWith(loadAllArtists())
                      .doOnCompleted { Debug.d "Artists loaded..." }
                      .concatWith(loadAllAlbums())
                      .doOnCompleted { Debug.d "Albums loaded..." }
                      .concatWith(writeCollectionIntoFile())
                      .doOnCompleted { Debug.d "Collection written to file." }
                      .concatWith(Observable.just(this))
    }

    Artist getArtist(Song song) {
        artists.get(song.artistId)
    }

    Album getAlbum(Song song) {
        albums.get(song.albumId)
    }

    Artist getArtist(Album album) {
        artists.get(album.artistId)
    }

    List<Song> getSongs(Album album) {
        songs.filter { it.albumId == album.id }
    }

    List<Song> getSongs(Artist artist) {
        songs.filter { it.artistId == artist.id }
    }

    List<Album> getAlbums(Artist artist) {
        albums.filter { it.artistId == artist.id }
    }

    private Observable<Song> loadAllSongs() {
        CursorConstructor.fromCursorGetter(Song, new SongsCursorGetter())
                         .onBackpressureBuffer()
                         .filter { it.source != null }
                         .doOnNext { songs.put(it.id, it) }
    }

    private Observable<Artist> loadAllArtists() {
        CursorConstructor.fromCursorGetter(Artist, new ArtistsCursorGetter())
                         .onBackpressureBuffer()
                         .filter { isArtistHasSongs(it) }
                         .doOnNext { artists.put(it.id, it) }
    }

    private Observable<Album> loadAllAlbums() {
        CursorConstructor.fromCursorGetter(Album, new AlbumsCursorGetter())
                         .onBackpressureBuffer()
                         .filter { isAlbumContainsSongs(it) }
                         .doOnNext { albums.put(it.id, it) }
    }

    private boolean isArtistHasSongs(Artist artist) {
        songs.first { it.artistId == artist.id } != null
    }

    private boolean isAlbumContainsSongs(Album album) {
        songs.first { it.albumId == album.id } != null
    }

    private Observable<Object> writeCollectionIntoFile() {
        Observable.create({ Subscriber<Object> subscriber ->
            def stream = context.openFileOutput("collection.lwm", Context.MODE_PRIVATE)

            new ObjectOutputStream(stream).withObjectOutputStream {
                it.writeObject(this as MusicCollection)
            }

            subscriber.onCompleted()
        } as Observable.OnSubscribe<Object>)
    }

    private Observable<Object> readAndInitCollectionFromFile() {
        Observable.create({ Subscriber<Object> subscriber ->
            def stream = context.openFileInput "collection.lwm"

            new ObjectInputStream(stream).withObjectInputStream {
                initWith(it.readObject() as MusicCollection)
            }

            subscriber.onCompleted()
        } as Observable.OnSubscribe<Object>)
    }

    private void clear() {
        songs.clear()
        albums.clear()
        artists.clear()
    }

    private void initWith(MusicCollection other) {
        songs = other.songs
        albums = other.albums
        artists = other.artists
    }
}