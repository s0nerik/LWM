package app.models
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.webkit.MimeTypeMap
import app.Config
import app.Daggered
import app.helpers.CollectionManager
import app.helpers.db.cursor_constructor.CursorInitializable
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.apache.commons.lang3.SerializationUtils

import javax.inject.Inject

import static android.provider.BaseColumns._ID
import static android.provider.MediaStore.Audio.AudioColumns.*
import static android.provider.MediaStore.MediaColumns.DATA
import static android.provider.MediaStore.MediaColumns.MIME_TYPE
import static android.provider.MediaStore.MediaColumns.TITLE

@EqualsAndHashCode(includes = ["id", "artistId", "albumId"])
@ToString
@CompileStatic
@Builder
@Parcelable(exclude = {metaClass; album; artist; albumArtUri; sourceUri; durationString; collectionManager})
class Song extends Daggered implements CursorInitializable, Serializable {

    @Inject
    @PackageScope
    transient CollectionManager collectionManager

    static final String[] SUPPORTED_MIME_TYPES = [
            MimeTypeMap.singleton.getMimeTypeFromExtension("mp3"),
            MimeTypeMap.singleton.getMimeTypeFromExtension("m4a"),
            MimeTypeMap.singleton.getMimeTypeFromExtension("mp4"),
            MimeTypeMap.singleton.getMimeTypeFromExtension("aac")
    ]

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

    String mimeType

    String getDurationString() {
        int seconds = duration / 1000 as int
        int minutes = seconds / 60 as int
        seconds -= minutes * 60
        minutes + ":" + String.format("%02d", seconds)
    }

    Uri getAlbumArtUri() {
        ContentUris.withAppendedId(artworkUri, albumId)
    }

    Uri getSourceUri() {
        Uri.parse("file://$source")
    }

    Album getAlbum() {
        collectionManager.getAlbum this
    }

    Artist getArtist() {
        collectionManager.getArtist this
    }

    byte[] serialize() {
        SerializationUtils.serialize this
    }

    static Song deserialize(byte[] bytes) {
        SerializationUtils.deserialize(bytes) as Song
    }

    String toJson() {
        JsonOutput.toJson([
                title: title,
                artist: artistName,
                album: albumName,
                source: source,
                lyrics: lyrics,
                duration: duration,
                albumArtUri: albumArtUri.toString(),
                mimeType: mimeType
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
        song.mimeType = jsonMap["mimeType"]

        return song
    }

    RemoteSong toRemoteSong(String host, String port = Config.HTTP_SERVER_PORT) {
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
        mimeType = cursor.getString indices[MIME_TYPE]
    }
}
