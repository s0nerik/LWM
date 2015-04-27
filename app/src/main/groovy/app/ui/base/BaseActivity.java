package app.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tale.prettybundle.PrettyBundle;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrettyBundle.inject(this);
    }
}
