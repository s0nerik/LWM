package app.helper
import app.model.Album
import app.model.Artist
import app.model.MusicCollection
import app.model.Song
import groovy.transform.CompileStatic
import groovy.transform.Memoized

@CompileStatic
class CollectionManager {

    @Delegate
    private MusicCollection collection = new MusicCollection()

    @Memoized
    List<Song> getSongs() {
        collection.songs.asList()
    }

    @Memoized
    List<Album> getAlbums() {
        collection.albums.asList()
    }

    @Memoized
    List<Artist> getArtists() {
        collection.artists.asList()
    }

}