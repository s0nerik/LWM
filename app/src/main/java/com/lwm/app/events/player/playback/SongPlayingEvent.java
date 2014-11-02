package com.lwm.app.events.player.playback;

public class SongPlayingEvent {

    private int progress;

    public SongPlayingEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }
}
