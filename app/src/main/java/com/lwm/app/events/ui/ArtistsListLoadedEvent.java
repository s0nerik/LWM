package com.lwm.app.events.ui;

import com.lwm.app.model.ArtistWrapperList;

public class ArtistsListLoadedEvent {

    private ArtistWrapperList list;

    public ArtistsListLoadedEvent(ArtistWrapperList list) {
        this.list = list;
    }

    public ArtistWrapperList getList() {
        return list;
    }
}
