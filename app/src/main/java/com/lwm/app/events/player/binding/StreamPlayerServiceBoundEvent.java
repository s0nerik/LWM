package com.lwm.app.events.player.binding;

import com.lwm.app.service.StreamPlayerService;

/**
 *
 * Created by sonerik on 7/25/14.
 */
public class StreamPlayerServiceBoundEvent {

    private StreamPlayerService streamPlayerService;

    public StreamPlayerServiceBoundEvent(StreamPlayerService streamPlayerService) {
        this.streamPlayerService = streamPlayerService;
    }

    public StreamPlayerService getStreamPlayerService() {
        return streamPlayerService;
    }
}
