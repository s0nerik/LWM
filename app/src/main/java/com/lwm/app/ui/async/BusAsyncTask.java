package com.lwm.app.ui.async;

import android.os.AsyncTask;

import com.lwm.app.Injector;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public abstract class BusAsyncTask<T> extends AsyncTask<Void, Void, T> {

    @Inject
    protected Bus bus;

    public BusAsyncTask() {
        Injector.inject(this);
    }
}
