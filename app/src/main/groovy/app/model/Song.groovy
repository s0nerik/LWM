package app.model

import android.content.ContentUris
import android.net.Uri
import android.util.JsonWriter
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.builder.Builder

@CompileStatic
@Builder
@Parcelable
class Song {

    protected static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart")

    long songId
    long artistId
    long albumId

    String title
    String artist
    String album
    String source
    String lyrics

    int duration

    String getDurationString() {
        int seconds = duration / 1000 as int
        int minutes = seconds / 60 as int
        seconds -= minutes * 60
        minutes + ":" + String.format("%02d", seconds)
    }

    @Memoized
    Uri getAlbumArtUri() {
        ContentUris.withAppendedId(artworkUri, albumId)
    }

    @Memoized
    Uri getSourceUri() {
        Uri.parse("file://$source")
    }

    String toJson() {
        JsonOutput.toJson([
                title: title,
                artist: artist,
                album: album,
                source: source,
                lyrics: lyrics,
                duration: duration,
                albumArtUri: albumArtUri.toString()
        ])
    }

    RemoteSong toRemoteSong(String serverUrl) {
        new RemoteSong(this, serverUrl)
    }

}
