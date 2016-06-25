package app

import android.app.Application
import app.di.AppComponent
import app.di.AppModule
import app.di.DaggerAppComponent
import com.crashlytics.android.Crashlytics
import groovy.transform.CompileStatic
import io.fabric.sdk.android.Fabric
import ru.noties.debug.Debug

@CompileStatic
final class App extends Application {
    private static App instance
    static App get() { instance }

    @Delegate
    private AppComponent comp

    @Override
    void onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.CRASHLYTICS)
            Fabric.with this, new Crashlytics()

        Debug.init BuildConfig.DEBUG

        initComponents()
    }

    private void initComponents() {
        comp = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build()
    }
}
