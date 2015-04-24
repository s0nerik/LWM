package com.lwm.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.lwm.app.modules.AndroidModule;

import ru.noties.debug.Debug;

public class App extends Application {

    public static final String TAG = "LWM";

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.init(new AndroidModule(this));
        if (BuildConfig.CRASHLYTICS) {
            Crashlytics.start(this);
        }
        Debug.init(BuildConfig.DEBUG);
    }

}
