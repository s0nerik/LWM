package app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.util.UUID;

public class BlurTransformation implements Transformation<Bitmap> {

    private static int MAX_RADIUS = 25;

    private Context mContext;
    private BitmapPool mBitmapPool;

    private int mRadius;

    public BlurTransformation(Context context, BitmapPool pool) {
        this(context, pool, MAX_RADIUS);
    }

    public BlurTransformation(Context context, BitmapPool pool, int radius) {
        mContext = context;
        mBitmapPool = pool;
        mRadius = radius;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();

        int width = source.getWidth();
        int height = source.getHeight();

//        Bitmap.Config config =
//                source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = mBitmapPool.get(width, height, config);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, config);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(source, 0, 0, null);

//        final Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
//        final Allocation output = Allocation.createTyped(rs, input.getType());
//        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//        script.setRadius(8f);
//        script.setInput(input);
//        script.forEach(output);
//        output.copyTo(bitmapOriginal);

        RenderScript rs = RenderScript.create(mContext);
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, input.getElement());
        blur.setInput(input);
        blur.setRadius(mRadius);
        blur.forEach(output);
        output.copyTo(bitmap);

        rs.destroy();

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    private ColorMatrix getDimMatrix(float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0,        0,        0, brightness,
                        0,        contrast, 0,        0, brightness,
                        0,        0,        contrast, 0, brightness,
                        0,        0,        0,        1, 0
                });
        return cm;
    }

    @Override
    public String getId() {
        return UUID.randomUUID().toString();
//        return "BlurTransformation(radius=" + mRadius + ")";
    }
}