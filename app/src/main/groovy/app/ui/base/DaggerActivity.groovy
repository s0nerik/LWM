package app.ui.base;

import android.os.Bundle;

import app.Injector
import groovy.transform.CompileStatic;

@CompileStatic
public abstract class DaggerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        Injector.inject this
    }
}
