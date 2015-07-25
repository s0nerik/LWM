package app.ui.base;

import android.os.Bundle;

import app.Injector
import groovy.transform.CompileStatic;

@CompileStatic
abstract class DaggerFragment extends BaseFragment {

    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        Injector.inject this
    }
}
