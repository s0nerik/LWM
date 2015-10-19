package app.ui.custom_view


import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.TextView

public class CheckedTextView extends TextView implements Checkable {

    private static final int[] CHECKED_STATE_SET = [
            android.R.attr.state_checked
    ] as int[]

    private boolean checked = false;

    CheckedTextView(Context context) {
        super(context)
    }

    CheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs)
    }

    CheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr)
    }

    CheckedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes)
    }

    @Override
    public boolean isChecked() {
        return checked
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked

        refreshDrawableState()

    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    @Override
    public void toggle() {
        this.@checked = !this.@checked
    }

    @Override
    public boolean performClick() {
        toggle()
        return super.performClick()
    }

}
