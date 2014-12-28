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
import com.lwm.app.Utils;

import javax.inject.Inject;

public class SingleBitmapPaletteInfoCallback implements FutureCallback<ImageViewBitmapInfo> {

    @Inject
    Resources resources;

    private View mLayout;
    private View mLayoutShadow;
    private TextView mTitle;
    private TextView mSubtitle;

    public SingleBitmapPaletteInfoCallback(View layout, TextView title, TextView subtitle) {
        mLayout = layout;
        mTitle = title;
        mSubtitle = subtitle;
        Injector.inject(this);
    }

    public SingleBitmapPaletteInfoCallback(View layout, View layoutShadow, TextView title, TextView subtitle) {
        this(layout, title, subtitle);
        mLayoutShadow = layoutShadow;
    }

    @Override
    public void onCompleted(Exception e, ImageViewBitmapInfo result) {
        BitmapInfo bitmapInfo = result.getBitmapInfo();
        if (result.getException() == null && bitmapInfo.bitmaps != null && bitmapInfo.bitmaps.length > 0) {
            Palette.generateAsync(bitmapInfo.bitmaps[0], new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch[] swatches = new Palette.Swatch[6];

                    swatches[0] = palette.getVibrantSwatch();
                    swatches[1] = palette.getDarkVibrantSwatch();
                    swatches[2] = palette.getMutedSwatch();
                    swatches[3] = palette.getDarkMutedSwatch();
                    swatches[4] = palette.getLightVibrantSwatch();
                    swatches[5] = palette.getLightMutedSwatch();

                    for (Palette.Swatch swatch : swatches) {
                        if (swatch != null) {
                            applyStyle(
                                    swatch.getRgb(),
                                    Utils.darkerColor(swatch.getRgb(), 0.8f),
                                    swatch.getTitleTextColor(),
                                    swatch.getBodyTextColor()
                            );
                            return;
                        }
                    }

                    applyDefaultStyle();
                }
            });
        } else {
            applyDefaultStyle();
        }
    }

    private void applyDefaultStyle() {
        applyStyle(
                resources.getColor(R.color.grid_item_default_bg),
                Utils.darkerColor(resources.getColor(R.color.grid_item_default_bg), 0.8f),
                Color.WHITE,
                Color.WHITE
        );
    }

    private void applyStyle(int bgColor, int shadowColor, int titleColor, int subtitleColor) {
        mLayout.setBackgroundColor(bgColor);
        if (mLayoutShadow != null) mLayoutShadow.setBackgroundColor(shadowColor);
        mTitle.setTextColor(titleColor);
        mSubtitle.setTextColor(subtitleColor);
    }

}
