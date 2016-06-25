package app.ui.base;

import android.os.Bundle;

import com.squareup.otto.Bus
import groovy.transform.CompileStatic;

import javax.inject.Inject;

@CompileStatic
public abstract class OttoOnCreateFragment extends BaseFragment {

    private Object[] busListeners = [ this ]

    @Inject
    protected Bus bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (Object o : busListeners) {
            bus.register(o);
        }
    }

    @Override
    public void onDestroy() {
        for (Object o : busListeners) {
            bus.unregister(o);
        }
        super.onDestroy();
    }

    public void setBusListeners(Object... busListeners) {
        this.busListeners = busListeners;
    }
}
