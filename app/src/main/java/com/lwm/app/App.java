package com.lwm.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lwm.app.event.server.StartServerRequestedEvent;
import com.lwm.app.event.server.StopServerRequestedEvent;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.service.MusicServerService;
import com.lwm.app.service.MusicService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formKey = "",
        formUri = "https://sonerik.cloudant.com/acra-lwm/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="baboommellowasthessizedi",
        formUriBasicAuthPassword="xGXD18c8AAuHq0jlopdtUNYJ",

        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)

public class App extends Application {

    public static final String TAG = "LWM";

    public static final String SERVICE_BOUND = "com.lwm.app.service_bound";

    private static MusicService musicService;
    private static boolean musicServiceBound = false;

    private Utils utils;

    public static MusicService getMusicService(){
        assert musicService != null : "musicService == null!";
        return musicService;
    }

    private static Bus eventBus;

    public static Bus getEventBus() {
        return eventBus;
    }

    public static boolean isMusicServiceBound() {
        return musicServiceBound;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Bind to MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);

        // Start ACRA
        ACRA.init(this);

        utils = new Utils(this);

        eventBus = new Bus(ThreadEnforcer.ANY);
        eventBus.register(this);
    }

    @Override
    public void onTerminate() {
        Log.d(App.TAG, "App: onTerminate");
        super.onTerminate();
        unbindService(musicServiceConnection);
        eventBus.unregister(this);
    }

    public static boolean localPlayerActive(){
        return musicServiceBound && musicService.localPlayerActive();
    }

    public static LocalPlayer getLocalPlayer(){
        return musicServiceBound ? musicService.getLocalPlayer() : null;
    }

    public static StreamPlayer getStreamPlayer(){
        return musicServiceBound ? musicService.getStreamPlayer() : null;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(App.TAG, "App: onServiceConnected");
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            musicService = binder.getService();
            musicServiceBound = true;
            sendBroadcast(new Intent().setAction(SERVICE_BOUND));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(App.TAG, "App: onServiceDisconnected");
            musicServiceBound = false;
        }
    };

    @Subscribe
    public void startServer(StartServerRequestedEvent event) {
        startService(new Intent(this, MusicServerService.class));
    }

    @Subscribe
    public void stopServer(StopServerRequestedEvent event) {
        stopService(new Intent(this, MusicServerService.class));
    }

}
