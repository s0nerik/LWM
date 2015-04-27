package app.ui.base;

import android.os.Bundle;

import app.Injector;

public abstract class DaggerFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }
}
