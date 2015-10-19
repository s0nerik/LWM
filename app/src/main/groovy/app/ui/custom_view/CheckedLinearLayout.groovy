package app.ui.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.LinearLayout
import groovy.transform.CompileStatic

@CompileStatic
public class CheckedLinearLayout extends LinearLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = [
            android.R.attr.state_checked
    ] as int[]

    private boolean checked = false

    CheckedLinearLayout(Context context) {
        super(context)
    }

    CheckedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs)
    }

    CheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr)
    }

    CheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes)
    }

    @Override
    void setChecked(boolean checked) {
        this.@checked = checked

        refreshDrawableState()

        //Propagate to children
        final int count = getChildCount()
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i)
            if (child instanceof Checkable) {
                (child as Checkable).setChecked checked
            }
        }
    }

    @Override
    boolean isChecked() {
        return this.@checked
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (this.@checked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    @Override
    void toggle() {
        this.@checked = !this.@checked
    }
}