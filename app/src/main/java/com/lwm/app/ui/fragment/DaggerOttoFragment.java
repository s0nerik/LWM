package com.lwm.app.ui.fragment;

import com.lwm.app.ui.base.DaggerFragment;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public abstract class DaggerOttoFragment extends DaggerFragment {

    @Inject
    protected Bus bus;

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }
}
