package com.lwm.app.ui.custom_view;

import android.content.Context;
import android.util.AttributeSet;

import com.dd.ShadowLayout;

public class SquareWidthShadowLayout extends ShadowLayout {

    public SquareWidthShadowLayout(Context context) {
        super(context);
    }

    public SquareWidthShadowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareWidthShadowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}