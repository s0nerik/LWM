package com.lwm.app.model;

import java.util.List;

public class ArtistWrapper {

    private Artist artist;
    private List<Album> albums;

    public ArtistWrapper(Artist artist) {
        this.artist = artist;
        albums = artist.getAlbums();
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public Artist getArtist() {
        return artist;
    }
}
