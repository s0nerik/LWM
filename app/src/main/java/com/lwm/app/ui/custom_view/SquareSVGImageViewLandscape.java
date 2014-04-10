package com.lwm.app.ui.custom_view;

import android.content.Context;
import android.util.AttributeSet;

import com.caverock.androidsvg.SVGImageView;

public class SquareSVGImageViewLandscape extends SVGImageView {

    public SquareSVGImageViewLandscape(Context context) {
        super(context);
    }

    public SquareSVGImageViewLandscape(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareSVGImageViewLandscape(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredWidth();
        setMeasuredDimension(height, height);
    }

}