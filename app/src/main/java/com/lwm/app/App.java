package com.lwm.app;

import android.app.Application;

import com.lwm.app.modules.AndroidModule;

public class App extends Application {

    public static final String TAG = "LWM";

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.init(new AndroidModule(this));
    }

}
