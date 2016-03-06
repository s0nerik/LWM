package app.events
import android.os.Handler
import android.os.Looper
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@InheritConstructors
@CompileStatic
class MainThreadBus extends Bus {
    private final Handler handler = new Handler(Looper.mainLooper)

    @Override
    void post(final Object event) {
        if (Looper.myLooper() == Looper.mainLooper) {
            super.post event
        } else {
            handler.post { super.post(event) }
        }
    }

}