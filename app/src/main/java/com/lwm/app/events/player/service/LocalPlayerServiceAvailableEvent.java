package com.lwm.app.events.player.service;

import com.lwm.app.service.LocalPlayerService;

public class LocalPlayerServiceAvailableEvent {

    private LocalPlayerService service;

    public LocalPlayerServiceAvailableEvent(LocalPlayerService service) {
        this.service = service;
    }

    public LocalPlayerService getService() {
        return service;
    }
}
