package com.lwm.app.ui.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareWidthImageView extends ImageView {

    public SquareWidthImageView(Context context) {
        super(context);
    }

    public SquareWidthImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareWidthImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}