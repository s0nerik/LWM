package app.ui.base

import android.os.Bundle
import com.trello.rxlifecycle.components.support.RxFragment
import groovy.transform.CompileStatic

@CompileStatic
abstract class BaseFragment extends RxFragment {
    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        initEventHandlersOnCreate()
    }

    @Override
    void onResume() {
        super.onResume()
        initEventHandlersOnResume()
    }

    protected void initEventHandlersOnCreate() {}
    protected void initEventHandlersOnResume() {}
}
