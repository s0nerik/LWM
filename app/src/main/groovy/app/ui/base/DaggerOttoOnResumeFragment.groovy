package app.ui.base;

import com.squareup.otto.Bus
import groovy.transform.CompileStatic;

import javax.inject.Inject;

@CompileStatic
public abstract class DaggerOttoOnResumeFragment extends DaggerFragment {

    private Object[] busListeners = { this };

    @Inject
    protected Bus bus;

    @Override
    public void onResume() {
        super.onResume();
        for (Object o : busListeners) {
            bus.register(o);
        }
    }

    @Override
    public void onPause() {
        for (Object o : busListeners) {
            bus.unregister(o);
        }
        super.onPause();
    }

    public void setBusListeners(Object... busListeners) {
        this.busListeners = busListeners;
    }
}
