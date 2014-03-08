package com.lwm.app.model;

public interface Playlist {

    public String getSource();
    public void next();
    public void prev();
    public void moveTo(int pos);
    public void setShuffle(boolean on);
    public String getCurrentTitle();
    public String getCurrentArtist();
    public String getCurrentAlbum();
    public int getCurrentDuration();
    public int getCurrentAlbumId();
    public int getCurrentListPosition();

}
