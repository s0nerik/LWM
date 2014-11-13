package com.lwm.app.events.ui;

import com.lwm.app.model.ArtistWrapperList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class ArtistsListLoadingEvent {

    public enum State {LOADING, LOADED}

    @Getter
    private ArtistWrapperList list;
    @Getter
    private final State state;
}
