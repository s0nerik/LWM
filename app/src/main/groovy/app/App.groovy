package app

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import app.modules.AndroidModule
import com.crashlytics.android.Crashlytics
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

@CompileStatic
public final class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.init(new AndroidModule(this))
        if (BuildConfig.CRASHLYTICS) {
            Crashlytics.start(this)
        }
        Debug.init(BuildConfig.DEBUG)
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
