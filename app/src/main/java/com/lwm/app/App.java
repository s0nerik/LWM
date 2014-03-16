package com.lwm.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lwm.app.service.MusicServerService;
import com.lwm.app.service.MusicService;

public class App extends Application {

    public static final String SERVER_ADDRESS = "http://192.168.43.1:8888";
    public static final String IP = "/ip/";
    public static final String CURRENT_POSITION = "/position";
    public static final String CURRENT_INFO = "/info";
    public static final String CURRENT_ALBUMART = "/albumart";
    public static final String STREAM = "/stream";
    public static final String PAUSE = "/pause";
    public static final String PLAY = "/play";
    public static final String SEEK_TO = "/seekTo/";
    public static final String CLIENT_READY = "/client_ready";
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
        super.onTerminate();
        unbindService(musicServiceConnection);
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
