package com.lwm.app.ui.base;

import android.os.Bundle;

import com.squareup.otto.Bus;

import javax.inject.Inject;

public abstract class DaggerOttoOnCreateFragment extends DaggerFragment {

    private Object[] busListeners = { this };

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
