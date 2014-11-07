package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lwm.app.Injector;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.control.ChangeSongEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.notification.NowPlayingNotification;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class LocalPlayerService extends Service {

    @Inject
    Bus bus;

    @Inject
    LocalPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        if (player.getServer().isStarted()) {
            player.stopServer();
        }

        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Produce
    public CurrentSongAvailableEvent produceCurrentSong() {
        return new CurrentSongAvailableEvent(player.getCurrentSong());
    }

    @Subscribe
    public void onChangeSongEvent(ChangeSongEvent event) {
        switch (event.getType()) {
            case NEXT:
                player.nextSong();
                break;
            case PREV:
                player.prevSong();
                break;
            case TOGGLE_PAUSE:
                player.togglePause();
                break;
        }
    }

    @Subscribe
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        startForeground(1337,
                new NowPlayingNotification(player.getCurrentSong()).create(true, false)
        );
    }

    @Subscribe
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        startForeground(1337,
                new NowPlayingNotification(player.getCurrentSong()).create(false, false)
        );
    }

    @Subscribe
    public void allClientsReady(AllClientsReadyEvent event) {
        player.start();
    }

    @Subscribe
    public void onStartClients(StartClientsEvent event) {
        player.start();
    }

    @Subscribe
    public void onPauseClients(PauseClientsEvent event) {
        player.pause();
    }

}
