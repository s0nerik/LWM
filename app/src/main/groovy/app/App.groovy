package app

import android.app.Application
import com.crashlytics.android.Crashlytics
import groovy.transform.CompileStatic
import io.fabric.sdk.android.Fabric
import ru.noties.debug.Debug

@CompileStatic
final class App extends Application {

    @Override
    void onCreate() {
        super.onCreate()
        if (BuildConfig.CRASHLYTICS)
            Fabric.with this, new Crashlytics()

        Debug.init BuildConfig.DEBUG

        Injector.init new AndroidModule(this)
    }

}
