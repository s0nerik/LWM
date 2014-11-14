package com.lwm.app.events.ui;

import com.lwm.app.model.Album;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class AlbumsListLoadingEvent {

    public enum State {LOADING, LOADED}

    @Getter
    private List<Album> list;
    @Getter
    private final State state;
}
