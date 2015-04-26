package com.lwm.app.model;

import android.content.ContentUris;
import android.net.Uri;

import hrisey.Parcelable;
import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
@Parcelable
public final class Song implements android.os.Parcelable {

    private static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");

    private long songId;
    private long artistId;
    private long albumId;

    private String title;
    private String artist;
    private String album;
    private String source;
    private String lyrics;

    private int duration;

    public String getDurationString() {
        int seconds = duration/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d", seconds);
    }

    public Uri getAlbumArtUri(){
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

}
