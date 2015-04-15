package com.lwm.app.events.ui;

import com.lwm.app.model.Song;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class QueueLoadingEvent {

    public enum State {LOADING, LOADED}

    @Getter
    private List<Song> list;
    @Getter
    private final State state;

}
