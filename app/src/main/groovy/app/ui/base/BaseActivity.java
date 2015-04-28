package app.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.arasthel.swissknife.SwissKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwissKnife.inject(this);
    }
}
