package app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.support.v8.renderscript.*
import app.Daggered
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
public class Blur extends Daggered {

    static final int RADIUS = 25

    @Inject
    @PackageScope
    Context context

    Observable<Bitmap> blurAsObservable(Bitmap input) {
        Observable.just(blur(input))
    }

    @SuppressLint("NewApi")
    Bitmap blur(Bitmap input) {
        try {
            RenderScript rs = RenderScript.create(context);
            Allocation alloc = Allocation.createFromBitmap(rs, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blur.setRadius(RADIUS);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rs, result);

            blur.forEach(outAlloc);

//            // Make greyscale
//            final ScriptIntrinsicColorMatrix scriptColor = ScriptIntrinsicColorMatrix.create(rs, Element.U8_4(rs));
//            scriptColor.setGreyscale();
//            scriptColor.forEach(outAlloc, outAlloc);

            outAlloc.copyTo(result);

            rs.destroy();
            return result;
        } catch (Exception e) {
            // TODO: handle exception
            return input;
        }

    }

}
