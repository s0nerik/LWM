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
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class LocalPlayerService extends Service {

    @Inject
    Bus bus;

    @Inject
    LocalPlayer player;

    private final LocalPlayerServiceBinder binder = new LocalPlayerServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(App.TAG, "LocalPlayerService: onCreate");

        Injector.inject(this);

        bus.register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(App.TAG, "LocalPlayerService: onDestroy");

        if (player.getServer().isStarted()) {
            player.stopServer();
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
