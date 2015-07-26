package app.ui
import android.content.res.Resources
import android.graphics.Color
import android.support.v7.graphics.Palette
import android.view.View
import android.widget.TextView
import app.Daggered
import app.R
import app.Utils
import com.melnykov.fab.FloatingActionButton
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.transform.PackageScope

import javax.inject.Inject

@InheritConstructors
@CompileStatic
class PaletteApplier extends Daggered {

    @Inject
    @PackageScope
    Resources resources

    PaletteApplier(float bgAlpha) {
        super()
        this.bgAlpha = bgAlpha
    }

    View layout
    View layoutShadow
    TextView title
    TextView subtitle
    FloatingActionButton fab

    Palette.Swatch[] swatches = new Palette.Swatch[6]

    private float bgAlpha = 1.0f;

    void apply(Palette palette) {
        if (palette) {
            swatches = [
                    palette.vibrantSwatch,
                    palette.mutedSwatch,
                    palette.lightMutedSwatch,
                    palette.lightVibrantSwatch,
                    palette.darkMutedSwatch,
                    palette.darkVibrantSwatch
            ] as Palette.Swatch[]
//            swatches[0] = palette.mutedSwatch
//            swatches[1] = palette.vibrantSwatch
//            swatches[2] = palette.lightMutedSwatch
//            swatches[3] = palette.lightVibrantSwatch
//            swatches[4] = palette.darkMutedSwatch
//            swatches[5] = palette.darkVibrantSwatch

            for (Palette.Swatch swatch : swatches) {
                if (swatch) {
                    applyStyle(
                            Utils.adjustAlpha(swatch.rgb, bgAlpha),
                            Utils.darkerColor(swatch.rgb, 0.8f),
                            Utils.stripAlpha(swatch.titleTextColor),
                            Utils.stripAlpha(swatch.bodyTextColor)
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
                Utils.adjustAlpha(resources.getColor(R.color.grid_item_no_cover_bg), bgAlpha),
                Utils.darkerColor(resources.getColor(R.color.grid_item_no_cover_bg), 0.8f),
                Color.WHITE,
                Color.WHITE
        )
    }

    private void applyStyle(int bgColor, int shadowColor, int titleColor, int subtitleColor) {
//        layout?.setBackgroundColor(bgColor)
//        fab?.setColorNormal(shadowColor)
        fab?.setColorNormal(bgColor)
        layoutShadow?.setBackgroundColor(shadowColor)
        title?.setTextColor(titleColor)
        subtitle?.setTextColor(subtitleColor)
    }

}