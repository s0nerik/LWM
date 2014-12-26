package com.lwm.app.ui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lwm.app.Injector;

public abstract class DaggerFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }
}
