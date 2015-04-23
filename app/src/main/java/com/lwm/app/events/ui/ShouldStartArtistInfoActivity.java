package com.lwm.app.events.ui;

import com.lwm.app.model.Artist;

public class ShouldStartArtistInfoActivity {

    private Artist artist;

    public ShouldStartArtistInfoActivity(Artist artist) {
        this.artist = artist;
    }

    public Artist getArtist() {
        return artist;
    }
}
