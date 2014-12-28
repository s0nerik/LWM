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
import com.melnykov.fab.FloatingActionButton;

import javax.inject.Inject;

import lombok.experimental.Builder;

public class SingleBitmapPaletteInfoCallback implements FutureCallback<ImageViewBitmapInfo> {

    @Inject
    Resources resources;

    private View layout;
    private View layoutShadow;
    private TextView title;
    private TextView subtitle;
    private FloatingActionButton floatingActionButton;

    public SingleBitmapPaletteInfoCallback(View layout, TextView title, TextView subtitle) {
        this.layout = layout;
        this.title = title;
        this.subtitle = subtitle;
        Injector.inject(this);
    }

    public SingleBitmapPaletteInfoCallback(View layout, View layoutShadow, TextView title, TextView subtitle) {
        this(layout, title, subtitle);
        this.layoutShadow = layoutShadow;
    }

    @Builder
    public SingleBitmapPaletteInfoCallback(View layout, View layoutShadow, TextView title, TextView subtitle, FloatingActionButton floatingActionButton) {
        this(layout, layoutShadow, title, subtitle);
        this.floatingActionButton = floatingActionButton;
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
                                    swatch.getTitleTextColor()
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
                resources.getColor(R.color.grid_item_no_cover_bg),
                Utils.darkerColor(resources.getColor(R.color.grid_item_no_cover_bg), 0.8f),
                Color.WHITE,
                Color.WHITE
        );
    }

    private void applyStyle(int bgColor, int shadowColor, int titleColor, int subtitleColor) {
        if (layout != null) layout.setBackgroundColor(bgColor);
        if (floatingActionButton != null) {
            floatingActionButton.setColorNormal(shadowColor);
            floatingActionButton.setColorNormal(bgColor);
        }
        if (layoutShadow != null) layoutShadow.setBackgroundColor(shadowColor);
        if (title != null) title.setTextColor(titleColor);
        if (subtitle != null) subtitle.setTextColor(subtitleColor);
    }

}
