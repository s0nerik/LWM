package app

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import app.modules.AndroidModule
import com.crashlytics.android.Crashlytics
import groovy.transform.CompileStatic
import io.fabric.sdk.android.Fabric
import ru.noties.debug.Debug

@CompileStatic
final class App extends MultiDexApplication {
//final class App extends Application {

    @Override
    void onCreate() {
        super.onCreate()
        if (BuildConfig.CRASHLYTICS)
            Fabric.with this, new Crashlytics()

        Debug.init BuildConfig.DEBUG

        Injector.init new AndroidModule(this)

//        RxJavaPlugins.instance.registerObservableExecutionHook(new DebugHook(new DebugNotificationListener() {
//            @Override
//            Object onNext(DebugNotification n) {
//                Debug.d("onNext on " + n)
//                return super.onNext(n)
//            }
//
//            @Override
//            Object start(DebugNotification n) {
//                Debug.d("start on " + n)
//                return super.start(n)
//            }
//
//            @Override
//            void complete(Object context) {
//                Debug.d("complete on " + context)
//                super.complete(context)
//            }
//
//            @Override
//            void error(Object context, Throwable e) {
//                Debug.d("error on " + context)
//                super.error(context, e)
//            }
//        }))
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
