package app

import android.support.multidex.MultiDexApplication
import app.modules.AndroidModule
import com.crashlytics.android.Crashlytics
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

@CompileStatic
final class App extends MultiDexApplication {

    @Override
    void onCreate() {
        super.onCreate()
        Injector.init new AndroidModule(this)
        if (BuildConfig.CRASHLYTICS) {
            Crashlytics.start this
        }
        Debug.init BuildConfig.DEBUG
    }

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base)
//        MultiDex.install(this)
//    }
}
