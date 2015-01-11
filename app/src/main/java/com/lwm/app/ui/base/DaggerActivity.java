package com.lwm.app.ui.base;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.lwm.app.Injector;

public abstract class DaggerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }
}
