package app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.lwm.app.BuildConfig;

import app.modules.AndroidModule;

import ru.noties.debug.Debug;

public class App extends Application {

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
