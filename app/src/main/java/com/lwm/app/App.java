package com.lwm.app;

import android.app.Application;
import android.content.Intent;

import com.lwm.app.service.MusicServerService;

public class App extends Application {

    public static final String SERVER_ADDRESS = "http://192.168.43.1:8888";
    public static final String IP = "/ip/";
    public static final String CURRENT_POSITION = "/position";
    public static final String CURRENT_INFO = "/info";
    public static final String STREAM = "/stream";
    public static final String PAUSE = "/pause";
    public static final String PLAY = "/play";
    public static final String SEEK_TO = "/seekTo/";
    public static final String CLIENT_READY = "/client_ready";
    public static final String TAG = "LWM";

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, MusicServerService.class));

    }

}
