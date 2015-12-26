package app.model
import android.content.Context
import android.support.v4.util.LongSparseArray
import app.Daggered
import app.data_managers.AlbumsManager
import app.data_managers.ArtistsManager
import app.data_managers.SongsManager
import app.helper.SerializableLongSparseArray
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import ru.noties.debug.Debug
import rx.Observable
import rx.Subscriber

import javax.inject.Inject

@CompileStatic
class MusicCollection extends Daggered implements Serializable {
    LongSparseArray<Song> songs = new SerializableLongSparseArray<>()
    LongSparseArray<Album> albums = new SerializableLongSparseArray<>()
    LongSparseArray<Artist> artists = new SerializableLongSparseArray<>()

    @Inject
    @PackageScope
    transient Context context

    Observable<Song> initFromFile() {
        Observable.create({ Subscriber<Song> subscriber ->
            clear()

            try {
                def stream = context.openFileInput "collection.lwm"
                new ObjectInputStream(stream).withObjectInputStream {
                    init(it.readObject() as MusicCollection)
                }

                for(int i = 0; i < songs.size(); i++) {
                    subscriber.onNext songs.valueAt(i)
                }

                subscriber.onCompleted()
            } catch (e) {
                subscriber.onError(e)
            }
        } as Observable.OnSubscribe<Song>)
    }

    Observable<Song> initFromMediaStore() {
        Observable.create({ Subscriber<Song> subscriber ->
            clear()

            Debug.d "Collection fetching..."
            def songsList = SongsManager.loadAllSongs().toList().toBlocking().single()
            Debug.d "Songs fetched"
            songsList.each { songs.put(it.id, it) }

            def artistsList = ArtistsManager.loadAllArtists().toList().toBlocking().single()
            Debug.d "Artists fetched"
            artistsList.each { artists.put(it.id, it) }

            def albumsList = AlbumsManager.loadAllAlbums { Album album -> songsList.find {it.albumId == album.id} != null }
                                          .toList()
                                          .toBlocking()
                                          .single()
            Debug.d "Albums fetched"
            albumsList.each { albums.put(it.id, it) }

            def stream = context.openFileOutput("collection.lwm", Context.MODE_PRIVATE)

            new ObjectOutputStream(stream).withObjectOutputStream {
                it.writeObject(this as MusicCollection)
            }
            Debug.d "Collection written"

            for(int i = 0; i < songs.size(); i++) {
                subscriber.onNext songs.valueAt(i)
            }

            subscriber.onCompleted()

        } as Observable.OnSubscribe<Song>)
    }

    Artist getArtist(Song song) {
        artists.get(song.artistId)
    }

    Artist getArtist(Album album) {
        artists.get(album.artistId)
    }

    Album getAlbum(Song song) {
        albums.get(song.albumId)
    }

    List<Song> getSongs(Album album) {
        def albumSongs = new ArrayList()
        for(int i = 0; i < songs.size(); i++) {
            def song = songs.valueAt(i)
            if (song.albumId == album.id) albumSongs << song
        }

        return albumSongs
    }

    List<Song> getSongs(Artist artist) {
        def artistSongs = new ArrayList()
        for(int i = 0; i < songs.size(); i++) {
            def song = songs.valueAt(i)
            if (song.artistId == artist.id) artistSongs << song
        }

        return artistSongs
    }

    private void clear() {
        songs.clear()
        albums.clear()
        artists.clear()
    }

    private void init(MusicCollection other) {
        songs = other.songs
        albums = other.albums
        artists = other.artists
    }
}