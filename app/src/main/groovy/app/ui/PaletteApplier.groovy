package app.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.graphics.Palette
import android.view.View
import android.widget.TextView
import app.Daggered
import app.R
import app.Utils
import com.melnykov.fab.FloatingActionButton
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor

import javax.inject.Inject

@CompileStatic
@TupleConstructor(callSuper = true)
class PaletteApplier extends Daggered {

    @Inject
    @PackageScope
    Resources resources

    Bitmap bitmap

    View layout
    View layoutShadow
    TextView title
    TextView subtitle
    FloatingActionButton fab

    void apply(Palette palette) {
        if (palette) {
            def swatches = new Palette.Swatch[6]

            swatches[0] = palette.getVibrantSwatch()
            swatches[1] = palette.getDarkVibrantSwatch()
            swatches[2] = palette.getMutedSwatch()
            swatches[3] = palette.getDarkMutedSwatch()
            swatches[4] = palette.getLightVibrantSwatch()
            swatches[5] = palette.getLightMutedSwatch()

            for (Palette.Swatch swatch : swatches) {
                if (swatch) {
                    applyStyle(
                            swatch.getRgb(),
                            Utils.darkerColor(swatch.getRgb(), 0.8f),
                            swatch.getTitleTextColor(),
                            swatch.getTitleTextColor()
                    )
                    return
                }
            }

            applyDefaultStyle()
        } else {
            applyDefaultStyle()
        }
    }

    private void applyDefaultStyle() {
        applyStyle(
                resources.getColor(R.color.grid_item_no_cover_bg),
                Utils.darkerColor(resources.getColor(R.color.grid_item_no_cover_bg), 0.8f),
                Color.WHITE,
                Color.WHITE
        )
    }

    private void applyStyle(int bgColor, int shadowColor, int titleColor, int subtitleColor) {
        layout?.setBackgroundColor(bgColor)
//        fab?.setColorNormal(shadowColor)
        fab?.setColorNormal(bgColor)
        layoutShadow?.setBackgroundColor(shadowColor)
        title?.setTextColor(titleColor)
        subtitle?.setTextColor(subtitleColor)
    }

}