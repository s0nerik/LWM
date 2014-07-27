package com.lwm.app.event.player.binding;

import com.lwm.app.service.LocalPlayerService;

/**
 * Created by sonerik on 7/25/14.
 */
public class LocalPlayerServiceBoundEvent {

    private LocalPlayerService localPlayerService;

    public LocalPlayerServiceBoundEvent(LocalPlayerService localPlayerService) {
        this.localPlayerService = localPlayerService;
    }

    public LocalPlayerService getLocalPlayerService() {
        return localPlayerService;
    }
}
