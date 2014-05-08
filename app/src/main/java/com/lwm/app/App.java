package com.lwm.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lwm.app.player.LocalPlayer;
import com.lwm.app.service.MusicServerService;
import com.lwm.app.service.MusicService;

public class App extends Application {

    public static final String TAG = "LWM";

    public static final String SERVICE_BOUND = "com.lwm.app.service_bound";

    private static MusicService musicService;
    private static boolean musicServiceBound = false;

    public static MusicService getMusicService(){
        assert musicService != null : "musicService == null!";
        return musicService;
    }

    public static boolean isMusicServiceBound() {
        return musicServiceBound;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, MusicServerService.class));

        // Bind to MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onTerminate() {
        Log.d(App.TAG, "App: onTerminate");
        super.onTerminate();
        unbindService(musicServiceConnection);
    }

    public static boolean localPlayerActive(){
        return musicServiceBound && musicService.localPlayerActive();
    }

    public static LocalPlayer getLocalPlayer(){
        return musicServiceBound ? musicService.getLocalPlayer() : null;
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

}
