package com.lwm.app.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
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
import com.lwm.app.helper.wifi.WifiAP;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.receiver.MediaButtonIntentReceiver;
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
    AudioManager audioManager;

    ComponentName mediaButtonsReceiver;

    @Override
    public void onCreate() {
        Injector.inject(this);

        if (wifiAP.isEnabled()) {
            startServer();
        }

        bus.register(this);

        mediaButtonsReceiver = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getCanonicalName());
        audioManager.registerMediaButtonEventReceiver(mediaButtonsReceiver);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        if (player.isPlaying()) {
            player.pause();
        }

        stopServer();
        bus.unregister(this);

        audioManager.unregisterMediaButtonEventReceiver(mediaButtonsReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player.hasCurrentSong()) {
            makeForeground(player.isPlaying());
        }
        return START_NOT_STICKY;
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
            case PLAY:
                if (!player.isPlaying()) {
                    player.start();
                }
                break;
            case PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                }
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
