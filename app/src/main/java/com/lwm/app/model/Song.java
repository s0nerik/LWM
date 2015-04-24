package com.lwm.app.model;

import android.content.ContentUris;
import android.net.Uri;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class Song {

    private static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");

    private final long songId;
    private final long artistId;
    private final long albumId;

    private final String title;
    private final String artist;
    private final String album;
    private final String source;
    private String lyrics;

    private final int duration;

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
