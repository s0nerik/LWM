package app.model
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import app.data_managers.AlbumsManager
import app.data_managers.ArtistsManager
import app.data_managers.CursorInitializable
import app.server.StreamServer
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import rx.Observable

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AudioColumns.*
import static android.provider.MediaStore.MediaColumns.DATA
import static android.provider.MediaStore.MediaColumns.TITLE

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass; artist; album})
class Song implements CursorInitializable {

    protected static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart")

    long id
    long artistId
    long albumId

    String title
    String artistName
    String albumName
    String source
    String lyrics

    int duration

    Song() {}

    String getDurationString() {
        int seconds = duration / 1000 as int
        int minutes = seconds / 60 as int
        seconds -= minutes * 60
        minutes + ":" + String.format("%02d", seconds)
    }

    Observable<Artist> getArtist() {
        ArtistsManager.loadArtistById(artistId)
    }

    Observable<Album> getAlbum() {
        AlbumsManager.loadAlbumById(albumId)
    }

    Uri getAlbumArtUri() {
        ContentUris.withAppendedId(artworkUri, albumId)
    }

    Uri getSourceUri() {
        Uri.parse("file://$source")
    }

    String toJson() {
        JsonOutput.toJson([
                title: title,
                artist: artistName,
                album: albumName,
                source: source,
                lyrics: lyrics,
                duration: duration,
                albumArtUri: albumArtUri.toString()
        ])
    }

    static Song fromJson(String json) {
        def jsonMap = new JsonSlurper().parseText(json)

        def song = new Song()
        song.title = jsonMap["title"]
        song.artistName = jsonMap["artist"]
        song.albumName = jsonMap["album"]
        song.source = jsonMap["source"]
        song.lyrics = jsonMap["lyrics"]
        song.duration = jsonMap["duration"] as int

        return song
    }

    RemoteSong toRemoteSong(String host, String port = StreamServer.PORT) {
        new RemoteSong(this, "http://$host:$port")
    }

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        id = cursor.getLong indices[_ID]
        artistId = cursor.getLong indices[ARTIST_ID]
        albumId = cursor.getLong indices[ALBUM_ID]
        title = cursor.getString indices[TITLE]
        artistName = cursor.getString indices[ARTIST]
        albumName = cursor.getString indices[ALBUM]
        source = cursor.getString indices[DATA]
        duration = cursor.getInt indices[DURATION]
    }
}
