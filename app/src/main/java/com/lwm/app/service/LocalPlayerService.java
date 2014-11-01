package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.Injector;
import com.lwm.app.events.access_point.StopServerEvent;
import com.lwm.app.events.player.service.CurrentSongAvailableEvent;
import com.lwm.app.events.server.AllClientsReadyEvent;
import com.lwm.app.events.server.PauseClientsEvent;
import com.lwm.app.events.server.StartClientsEvent;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.server.MusicServer;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class LocalPlayerService extends Service {

    @Inject
    Bus bus;

    private LocalPlayer player;

    private MusicServer server;

    private final LocalPlayerServiceBinder binder = new LocalPlayerServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(App.TAG, "LocalPlayerService: onCreate");

        Injector.inject(this);

        player = new LocalPlayer(this);

        bus.register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(App.TAG, "LocalPlayerService: onDestroy");

        if (isServerStarted()) {
            stopServer();
        }

        bus.post(new StopServerEvent());
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalPlayerServiceBinder extends Binder {
        public LocalPlayerService getService() {
            return LocalPlayerService.this;
        }

        public LocalPlayer getPlayer() {
            return player;
        }
    }

    public boolean isServerStarted() {
        return server != null;
    }

    public void startServer() {
        server = new MusicServer(player);
        server.start();
    }

    public void stopServer() {
        server.stop();
        server = null;
    }

    @Produce
    public CurrentSongAvailableEvent produceCurrentSong() {
        return new CurrentSongAvailableEvent(player.getCurrentSong());
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
