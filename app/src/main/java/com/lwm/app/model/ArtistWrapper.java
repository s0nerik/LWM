package com.lwm.app.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ArtistWrapper {
    private final Artist artist;
    private List<Album> albums;
}
