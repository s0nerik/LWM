package app.events.ui;

import app.model.Artist;

public class ShouldStartArtistInfoActivity {

    private Artist artist;

    public ShouldStartArtistInfoActivity(Artist artist) {
        this.artist = artist;
    }

    public Artist getArtist() {
        return artist;
    }
}
