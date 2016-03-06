package app.ui.custom_view;

import android.content.Context;
import android.util.AttributeSet;

import com.flaviofaria.kenburnsview.KenBurnsView;

public class SquareWidthKenBurnsView extends KenBurnsView {

    public SquareWidthKenBurnsView(Context context) {
        super(context);
    }

    public SquareWidthKenBurnsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareWidthKenBurnsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
