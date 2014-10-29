package com.lwm.app.events.player.service;

import com.lwm.app.player.LocalPlayer;

public class LocalPlayerServiceConnectedEvent {

    private LocalPlayer player;

    public LocalPlayerServiceConnectedEvent(LocalPlayer player) {
        this.player = player;
    }

    public LocalPlayer getPlayer() {
        return player;
    }
}
