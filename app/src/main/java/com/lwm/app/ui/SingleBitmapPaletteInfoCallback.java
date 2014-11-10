package com.lwm.app.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.lwm.app.Injector;
import com.lwm.app.R;

import javax.inject.Inject;

public class SingleBitmapPaletteInfoCallback implements FutureCallback<ImageViewBitmapInfo> {

    @Inject
    Resources resources;

    private View mLayout;
    private TextView mTitle;
    private TextView mSubtitle;

    public SingleBitmapPaletteInfoCallback(View layout, TextView title, TextView subtitle) {
        mLayout = layout;
        mTitle = title;
        mSubtitle = subtitle;
        Injector.inject(this);
    }

    @Override
    public void onCompleted(Exception e, ImageViewBitmapInfo result) {
        BitmapInfo bitmapInfo = result.getBitmapInfo();
        if (result.getException() == null && bitmapInfo.bitmaps != null && bitmapInfo.bitmaps.length > 0) {
            Palette.generateAsync(bitmapInfo.bitmaps[0], new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatch = palette.getVibrantSwatch();
                    if (swatch != null) {
                        applyStyle(
                                swatch.getRgb(),
                                swatch.getTitleTextColor(),
                                swatch.getBodyTextColor()
                        );
                    } else {
                        applyDefaultStyle();
                    }
                }
            });
        } else {
            applyDefaultStyle();
        }
    }

    private void applyDefaultStyle() {
        applyStyle(resources.getColor(R.color.now_playing_bg), Color.WHITE, Color.WHITE);
    }

    private void applyStyle(int bgColor, int titleColor, int subtitleColor) {
        mLayout.setBackgroundColor(bgColor);
        mTitle.setTextColor(titleColor);
        mSubtitle.setTextColor(subtitleColor);
    }

}
