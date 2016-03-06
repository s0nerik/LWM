package app.ui
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor(force = true)
@CompileStatic
class PaletteBitmapImageViewTarget extends BitmapImageViewTarget {

    Resources resources

    View layout
    View layoutShadow
    TextView title
    TextView subtitle

    PaletteBitmapImageViewTarget(ImageView view) {
        super(view)
    }

    @Override
    void onLoadFailed(Exception e, Drawable errorDrawable) {
        super.onLoadFailed(e, errorDrawable)
//        new PaletteApplier(resources: resources).apply(null)
    }

    @Override
    void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
        super.onResourceReady(resource, glideAnimation)
//        new PaletteApplier(resources: resources, title: title, subtitle: subtitle, layout: layout, layoutShadow: layoutShadow)
//                .apply(resource)
    }
}