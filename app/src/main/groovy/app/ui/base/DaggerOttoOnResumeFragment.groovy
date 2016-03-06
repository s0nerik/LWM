package app.ui.base;

import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope;

import javax.inject.Inject;

@CompileStatic
abstract class DaggerOttoOnResumeFragment extends DaggerFragment {

    private Object[] busListeners = [ this ]

    @Inject
    @PackageScope
    Bus bus

    @Override
    void onResume() {
        super.onResume()
        for (Object o : busListeners) {
            bus.register(o)
        }
    }

    @Override
    void onPause() {
        for (Object o : busListeners) {
            bus.unregister(o)
        }
        super.onPause()
    }

    void setBusListeners(Object... busListeners) {
        this.busListeners = busListeners
    }
}
