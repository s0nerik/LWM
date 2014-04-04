package com.lwm.app.model;

public class Album {
    private int id;
    private String title;
    private String artist;
    private int year;
    private int songsCount;
    private String albumArtUri;

    public Album(String title) {
        this.title = title;
    }

    public Album(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public Album(String title, String artist, int year) {
        this.title = title;
        this.artist = artist;
        this.year = year;
    }

    public Album(String title, String artist, int year, String albumArtUri) {
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.albumArtUri = albumArtUri;
    }

    public Album(String title, String artist, int year, String albumArtUri, int songsCount) {
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.albumArtUri = albumArtUri;
        this.songsCount = songsCount;
    }

    public Album(int id, String title, String artist, int year, String albumArtUri, int songsCount) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.albumArtUri = albumArtUri;
        this.songsCount = songsCount;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getYear() {
        return year;
    }

    public int getSongsCount() {
        return songsCount;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public int getId() {
        return id;
    }
}
