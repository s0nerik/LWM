package com.lwm.app.model;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class Album {
    private int id;
    private String title;
    private String artist;
    private int year;
    private int songsCount;
    private String albumArtPath;
}
