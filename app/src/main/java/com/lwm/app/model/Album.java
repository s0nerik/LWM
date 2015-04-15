package com.lwm.app.model;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Album {
    private int id;
    private String title;
    private String artist;
    private int year;
    private int songsCount;
    private String albumArtPath;

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

    public Album(String title, String artist, int year, String albumArtPath) {
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.albumArtPath = albumArtPath;
    }

    public Album(String title, String artist, int year, String albumArtPath, int songsCount) {
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.albumArtPath = albumArtPath;
        this.songsCount = songsCount;
    }

    public Album(int id, String title, String artist, int year, String albumArtPath, int songsCount) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.albumArtPath = albumArtPath;
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

    public String getAlbumArtPath() {
        return albumArtPath;
    }

    public int getId() {
        return id;
    }
}
