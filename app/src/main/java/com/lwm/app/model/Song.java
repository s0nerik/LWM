package com.lwm.app.model;

import android.content.ContentUris;
import android.net.Uri;

public class Song {

    private static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");

    private String title;
    private String artist;
    private String album;
    private String lyrics;
    private String source;

    private long songId;
    private long artistId;
    private long albumId;

    private int duration;

    public Song(long songId, long artistId, long albumId, String title, String artist, String album, String source, int duration) {
        this.songId = songId;
        this.artistId = artistId;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.source = source;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDurationString() {
        int seconds = duration/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        return minutes+":"+String.format("%02d", seconds);
    }

    public Uri getAlbumArtUri(){
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (duration != song.duration) return false;
        if (songId != song.songId) return false;
        if (album != null ? !album.equals(song.album) : song.album != null) return false;
        if (artist != null ? !artist.equals(song.artist) : song.artist != null) return false;
        if (lyrics != null ? !lyrics.equals(song.lyrics) : song.lyrics != null) return false;
        if (!source.equals(song.source)) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (lyrics != null ? lyrics.hashCode() : 0);
        result = 31 * result + source.hashCode();
        result = 31 * result + (int) (songId ^ (songId >>> 32));
        result = 31 * result + (int) (artistId ^ (artistId >>> 32));
        result = 31 * result + (int) (albumId ^ (albumId >>> 32));
        result = 31 * result + duration;
        return result;
    }

    //    @Override
//    public int hashCode() {
//        return (int)(songId-(songId >>> 32));
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        return o.hashCode() == hashCode();
//    }
}
