package com.lwm.app.ui.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.lwm.app.App;
import com.lwm.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BroadcastButton extends RelativeLayout {

    @InjectView(R.id.icon)
    ImageView mIcon;
    @InjectView(R.id.progress)
    ProgressBar mProgress;

    public BroadcastButton(Context context) {
        super(context);
        init();
    }

    public BroadcastButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BroadcastButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.ab_item_broadcast, this);
        ButterKnife.inject(this, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(App.TAG, "BroadcastButton onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(App.TAG, "BroadcastButton onDetachedFromWindow");
    }
}
