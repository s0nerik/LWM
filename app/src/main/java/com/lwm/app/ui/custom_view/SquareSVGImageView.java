package com.lwm.app.ui.custom_view;

import android.content.Context;
import android.util.AttributeSet;

import com.caverock.androidsvg.SVGImageView;

public class SquareSVGImageView extends SVGImageView {

    public SquareSVGImageView(Context context) {
        super(context);
    }

    public SquareSVGImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareSVGImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        setMeasuredDimension(height, height);
    }

}