package com.lwm.app.ui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.hannesdorfmann.fragmentargs.FragmentArgs;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this);
    }
}
