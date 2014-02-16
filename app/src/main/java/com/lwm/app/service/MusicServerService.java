package com.lwm.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lwm.app.model.StreamServer;

import java.io.IOException;

public class MusicServerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            new StreamServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
