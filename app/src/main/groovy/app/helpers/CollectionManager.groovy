package app.helpers
import app.models.Album
import app.models.Artist
import app.models.MusicCollection
import app.models.Song
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