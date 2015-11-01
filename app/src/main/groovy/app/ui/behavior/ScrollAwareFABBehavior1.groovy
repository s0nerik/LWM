package app.ui.behavior

import android.content.Context
import android.support.design.widget.*
import android.util.AttributeSet
import android.view.View
import app.Utils
import groovy.transform.CompileStatic

@CompileStatic
public class ScrollAwareFABBehavior1 extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private int toolbarHeight;

    public ScrollAwareFABBehavior1(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.toolbarHeight = Utils.dpToPx(56)
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        if (dependency instanceof AppBarLayout) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            int distanceToScroll = fab.getHeight() + fabBottomMargin;
            float ratio = (float)dependency.getY()/(float)toolbarHeight as float
            fab.setTranslationY(-distanceToScroll * ratio as float);
        }
        return true;
    }
}