package com.lwm.app.events.player.playback;

public class SongPlayingEvent {

    private int progress;
    private int duration;

    public SongPlayingEvent(int progress, int duration) {
        this.progress = progress;
        this.duration = duration;
    }

    public int getProgress() {
        return progress;
    }

    public int getDuration() {
        return duration;
    }
}
