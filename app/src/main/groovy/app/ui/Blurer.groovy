package app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import app.App
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import groovy.transform.CompileStatic
import jp.wasabeef.glide.transformations.BlurTransformation
import rx.Observable

import javax.inject.Inject

@CompileStatic
class Blurer {

    static final int RADIUS = 25

    @Inject
    protected Context context

    Blurer() {
        App.get().inject this
    }

    @SuppressLint("NewApi")
    Bitmap blur(Bitmap input) {
        return new BlurTransformation(context, RADIUS).transform(new BitmapResource(input, Glide.get(context).getBitmapPool()), input.width, input.height).get();
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                RenderScript rs = RenderScript.create(context);
//                Allocation alloc = Allocation.createFromBitmap(rs, input);
//
//                ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//                blur.setRadius(RADIUS);
//                blur.setInput(alloc);
//
//                Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
//                Allocation outAlloc = Allocation.createFromBitmap(rs, result);
//
//                blur.forEach(outAlloc);
//
////            // Make greyscale
////            final ScriptIntrinsicColorMatrix scriptColor = ScriptIntrinsicColorMatrix.create(rs, Element.U8_4(rs));
////            scriptColor.setGreyscale();
////            scriptColor.forEach(outAlloc, outAlloc);
//
//                outAlloc.copyTo(result);
//
//                rs.destroy();
//                return result;
//            } else {
//                return NativeStackBlur.process(input, RADIUS);
//            }
//        } catch (Exception e) {
//            // TODO: handle exception
//            return input;
//        }

    }

    @SuppressLint("NewApi")
    Observable<Bitmap> blurAsObservable(Bitmap input) {
        Observable.fromCallable { blur(input) }
    }

}
