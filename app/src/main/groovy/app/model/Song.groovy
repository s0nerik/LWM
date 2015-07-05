package app.model

import android.content.ContentUris
import android.net.Uri
import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder

@CompileStatic
@Builder
@Parcelable
public final class Song {

    private static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart")

    long songId
    long artistId
    long albumId

    String title
    String artist
    String album
    String source
    String lyrics

    int duration

    public String getDurationString() {
        int seconds = duration / 1000 as int;
        int minutes = seconds / 60 as int;
        seconds -= minutes * 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    public Uri getAlbumArtUri() {
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

}
