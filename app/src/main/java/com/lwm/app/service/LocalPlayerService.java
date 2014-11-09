package com.lwm.app.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lwm.app.Injector;
import com.lwm.app.events.access_point.AccessPointStateEvent;
import com.lwm.app.events.player.playback.PlaybackPausedEvent;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.events.player.playback.control.ChangeSongEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.lib.WifiAP;
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

    @Inject
    WifiAP wifiAP;

    @Inject
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.inject(this);

        if (wifiAP.isEnabled()) {
            startServer();
        }

        bus.register(this);
    }

    @Override
    public void onDestroy() {
        stopServer();
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player.getCurrentSong() != null) {
            makeForeground(player.isPlaying());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void makeForeground(boolean isPlaying) {
        startForeground(1337,
                new NowPlayingNotification(player.getCurrentSong()).create(isPlaying)
        );
    }

    private void stopServer() {
        if (player.getServer().isStarted()) {
            player.stopServer();
        }
    }

    private void startServer() {
        player.startServer();
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
    public void onApStateChanged(AccessPointStateEvent event) {
        switch (event.getState()) {
            case DISABLED:
                stopServer();
                break;
            case ENABLED:
                startServer();
                break;
        }
    }

    @Subscribe
    public void onPlaybackStarted(PlaybackStartedEvent event) {
        makeForeground(true);
    }

    @Subscribe
    public void onPlaybackPaused(PlaybackPausedEvent event) {
        makeForeground(false);
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
