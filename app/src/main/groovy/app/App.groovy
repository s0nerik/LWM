package app

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import app.modules.AndroidModule
import com.crashlytics.android.Crashlytics
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

@CompileStatic
final class App extends MultiDexApplication {
//final class App extends Application {

    @Override
    void onCreate() {
        super.onCreate()
        if (BuildConfig.CRASHLYTICS)
            Crashlytics.start this

        Debug.init BuildConfig.DEBUG

        Injector.init new AndroidModule(this)
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
