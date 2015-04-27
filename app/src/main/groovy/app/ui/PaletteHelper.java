package app.ui;

import android.support.v7.graphics.Palette;

public class PaletteHelper {

    public static Palette.Swatch[] getSwatches(Palette palette) {
        Palette.Swatch[] swatches = new Palette.Swatch[6];

        swatches[0] = palette.getVibrantSwatch();
        swatches[1] = palette.getDarkVibrantSwatch();
        swatches[2] = palette.getMutedSwatch();
        swatches[3] = palette.getDarkMutedSwatch();
        swatches[4] = palette.getLightVibrantSwatch();
        swatches[5] = palette.getLightMutedSwatch();

        return swatches;
    }

    public static Palette.Swatch getFirstSwatch(Palette palette) {
        Palette.Swatch[] swatches = getSwatches(palette);

        for (Palette.Swatch swatch : swatches) {
            if (swatch != null) return swatch;
        }

        return null;
    }

}
