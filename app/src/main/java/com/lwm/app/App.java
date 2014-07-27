package com.lwm.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lwm.app.event.access_point.AccessPointDisabledEvent;
import com.lwm.app.event.access_point.AccessPointEnabledEvent;
import com.lwm.app.event.player.StartForegroundLocalPlayerEvent;
import com.lwm.app.event.player.binding.BindLocalPlayerServiceEvent;
import com.lwm.app.event.player.binding.BindStreamPlayerServiceEvent;
import com.lwm.app.event.player.binding.LocalPlayerServiceBoundEvent;
import com.lwm.app.event.player.binding.StreamPlayerServiceBoundEvent;
import com.lwm.app.event.player.binding.UnbindLocalPlayerServiceEvent;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.service.MusicServerService;
import com.lwm.app.service.StreamPlayerService;
import com.lwm.app.ui.notification.NowPlayingNotification;
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

    private static LocalPlayerService localPlayerService;
    private static StreamPlayerService streamPlayerService;
    private static boolean localPlayerServiceBound = false;
    private static boolean streamPlayerServiceBound = false;
    private static boolean serverStarted = false;

    public static LocalPlayerService getLocalPlayerService(){
        assert localPlayerService != null : "localPlayerService == null!";
        return localPlayerService;
    }

    public static StreamPlayerService getStreamPlayerService(){
        assert streamPlayerService != null : "streamPlayerService == null!";
        return streamPlayerService;
    }

    private static Bus eventBus;

    public static Bus getEventBus() {
        return eventBus;
    }

    public static boolean isLocalPlayerServiceBound() {
        return localPlayerServiceBound;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start ACRA
        ACRA.init(this);

        eventBus = new Bus(ThreadEnforcer.ANY);
        eventBus.register(this);
    }

    @Override
    public void onTerminate() {
        Log.d(App.TAG, "App: onTerminate");
        unbindService(localPlayerServiceConnection);
        eventBus.unregister(this);
        super.onTerminate();
    }

    public static boolean localPlayerActive(){
        return localPlayerServiceBound;
    }

    private ServiceConnection localPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(App.TAG, "LOCAL_PLAYER: onServiceConnected");
            LocalPlayerService.LocalPlayerServiceBinder binder = (LocalPlayerService.LocalPlayerServiceBinder) service;
            localPlayerService = binder.getService();
            localPlayerServiceBound = true;
            eventBus.post(new LocalPlayerServiceBoundEvent(localPlayerService));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(App.TAG, "LOCAL_PLAYER: onServiceDisconnected");
            localPlayerServiceBound = false;
        }
    };

    private ServiceConnection streamPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(App.TAG, "STREAM_PLAYER: onServiceConnected");
            StreamPlayerService.StreamPlayerServiceBinder binder = (StreamPlayerService.StreamPlayerServiceBinder) service;
            streamPlayerService = binder.getService();
            streamPlayerServiceBound = true;
            eventBus.post(new StreamPlayerServiceBoundEvent(streamPlayerService));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(App.TAG, "STREAM_PLAYER: onServiceDisconnected");
            streamPlayerServiceBound = false;
        }
    };

    @Subscribe
    public void bindLocalPlayerService(BindLocalPlayerServiceEvent event) {
        if (streamPlayerServiceBound) {
            unbindService(streamPlayerServiceConnection);
            streamPlayerServiceBound = false;
            streamPlayerService = null;
        }

        if (localPlayerServiceBound) {
            eventBus.post(new LocalPlayerServiceBoundEvent(localPlayerService));
        } else {
            Intent intent = new Intent(this, LocalPlayerService.class);
            bindService(intent, localPlayerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Subscribe
    public void bindStreamPlayerService(BindStreamPlayerServiceEvent event) {
        if (localPlayerServiceBound) unbindLocalPlayerService(null);

        if (streamPlayerServiceBound) {
            eventBus.post(new StreamPlayerServiceBoundEvent(streamPlayerService));
        } else {
            Intent intent = new Intent(this, StreamPlayerService.class);
            bindService(intent, streamPlayerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Subscribe
    public void unbindLocalPlayerService(UnbindLocalPlayerServiceEvent event) {
        unbindService(localPlayerServiceConnection);
        localPlayerServiceBound = false;
        localPlayerService = null;
    }

    @Subscribe
    public void startForegroundLocalPlayer(StartForegroundLocalPlayerEvent event) {
        if (localPlayerServiceBound) {
            localPlayerService.startForeground(0, NowPlayingNotification.create(this));
        }
    }

    @Subscribe
    public void startServer(AccessPointEnabledEvent event) {
        startService(new Intent(this, MusicServerService.class));
        serverStarted = true;
    }

    @Subscribe
    public void stopServer(AccessPointDisabledEvent event) {
        stopService(new Intent(this, MusicServerService.class));
        serverStarted = false;
    }

    public static boolean isServerStarted() {
        return serverStarted;
    }
}
