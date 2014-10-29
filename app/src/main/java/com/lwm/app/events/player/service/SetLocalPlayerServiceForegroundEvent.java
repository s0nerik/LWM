package com.lwm.app.events.player.service;

public class SetLocalPlayerServiceForegroundEvent {

    private boolean foreground;

    public SetLocalPlayerServiceForegroundEvent(boolean foreground) {
        this.foreground = foreground;
    }

    public boolean isForeground() {
        return foreground;
    }
}
