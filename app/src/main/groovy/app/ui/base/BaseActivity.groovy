package app.ui.base

import android.os.Bundle
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import groovy.transform.CompileStatic

@CompileStatic
public abstract class BaseActivity extends RxAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        initEventHandlersOnCreate()
    }

    @Override
    protected void onResume() {
        super.onResume()
        initEventHandlersOnResume()
    }

    protected void initEventHandlersOnCreate() {}
    protected void initEventHandlersOnResume() {}
}
