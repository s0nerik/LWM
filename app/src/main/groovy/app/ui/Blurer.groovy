package app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import app.Daggered
import com.commit451.nativestackblur.NativeStackBlur
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Observable

import javax.inject.Inject

@CompileStatic
class Blurer extends Daggered {

    static final int RADIUS = 25

    @Inject
    @PackageScope
    Context context

    @SuppressLint("NewApi")
    Bitmap blur(Bitmap input) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
            } else {
                return NativeStackBlur.process(input, RADIUS);
            }
        } catch (Exception e) {
            // TODO: handle exception
            return input;
        }

    }

    @SuppressLint("NewApi")
    Observable<Bitmap> blurAsObservable(Bitmap input) {
        Observable.fromCallable { blur(input) }
    }

}
