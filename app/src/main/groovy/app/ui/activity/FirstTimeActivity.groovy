package app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import app.R
import groovy.transform.CompileStatic;

@CompileStatic
public class FirstTimeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);
    }
}
