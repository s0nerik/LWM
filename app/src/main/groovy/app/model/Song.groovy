package app.model

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import app.data_managers.CursorInitializable
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.builder.Builder

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AudioColumns.*
import static android.provider.MediaStore.MediaColumns.DATA
import static android.provider.MediaStore.MediaColumns.TITLE

@CompileStatic
@Builder
@Parcelable
class Song implements CursorInitializable {

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

    Song() {}

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

    @Override
    void initialize(Cursor cursor, Map<String, Integer> indices) {
        songId = cursor.getLong indices[_ID]
        artistId = cursor.getLong indices[ARTIST_ID]
        albumId = cursor.getLong indices[ALBUM_ID]
        title = cursor.getString indices[TITLE]
        artist = cursor.getString indices[ARTIST]
        album = cursor.getString indices[ALBUM]
        source = cursor.getString indices[DATA]
        duration = cursor.getInt indices[DURATION]
    }
}
