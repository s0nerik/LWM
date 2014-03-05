//package com.lwm.app.model;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//public class SongParcelable implements Parcelable {
//
//    private String name;
//    private String artist;
//    private String album;
//    private String lyrics;
//    private String source;
//
//    public SongParcelable(){}
//
//    public SongParcelable(String name, String artist, String album, String source) {
//        this.name = name;
//        this.artist = artist;
//        this.album = album;
//        this.source = source;
//    }
//
//    public SongParcelable(String name, String artist, String album, String source, String lyrics) {
//        this.name = name;
//        this.artist = artist;
//        this.album = album;
//        this.source = source;
//        this.lyrics = lyrics;
//    }
//
//    private SongParcelable(Parcel in) {
//        readFromParcel(in);
//    }
//
//    public static final Creator<SongParcelable> CREATOR = new
//            Creator<SongParcelable>() {
//                public SongParcelable createFromParcel(Parcel in) {
//                    return new SongParcelable(in);
//                }
//
//                public SongParcelable[] newArray(int size) {
//                    return new SongParcelable[size];
//                }
//            };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//        parcel.writeString(name);
//        parcel.writeString(artist);
//        parcel.writeString(album);
//        parcel.writeString(lyrics);
//        parcel.writeString(source);
//    }
//
//    private void readFromParcel(Parcel in) {
//        name = in.readString();
//        artist = in.readString();
//        album = in.readString();
//        lyrics = in.readString();
//        source = in.readString();
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getArtist() {
//        return artist;
//    }
//
//    public void setArtist(String artist) {
//        this.artist = artist;
//    }
//
//    public String getAlbum() {
//        return album;
//    }
//
//    public void setAlbum(String album) {
//        this.album = album;
//    }
//
//    public String getLyrics() {
//        return lyrics;
//    }
//
//    public void setLyrics(String lyrics) {
//        this.lyrics = lyrics;
//    }
//
//    public String getSource() {
//        return source;
//    }
//
//    public void setSource(String source) {
//        this.source = source;
//    }
//
//}