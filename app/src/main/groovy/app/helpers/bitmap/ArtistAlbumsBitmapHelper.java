//package app.helper.bitmap;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Point;
//import android.media.ThumbnailUtils;
//import android.view.WindowManager;
//
//import com.bumptech.glide.Glide;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//
//import app.Injector;
//import app.R;
//import app.model.Album;
//import app.model.Artist;
//
//public class ArtistAlbumsBitmapHelper {
//
//    @Inject
//    WindowManager windowManager;
//
//    @Inject
//    Context context;
//
//    @Inject
//    Resources resources;
//
//    private static final int MAX_ITEMS = 5;
//    private static final int DIVIDER = 4;
//
//    private static int[] dividers;
//
//    private Artist artist;
//
//    private int thumbnailSize;
//
//    private Bitmap shadow;
//
//    public ArtistAlbumsBitmapHelper(Artist artist) {
//        App.get().inject(this);
//        this.artist = artist;
//        initThumbnailSize();
//        initShadow();
//    }
//
//    private void initThumbnailSize() {
//        Point point = new Point();
//        windowManager.getDefaultDisplay().getSize(point);
//        thumbnailSize = point.x / 2;
//    }
//
//    private void initShadow() {
//        Bitmap originalShadow = BitmapFactory.decodeResource(resources, R.drawable.album_shadow);
//        shadow = Bitmap.createScaledBitmap(
//                originalShadow,
//                thumbnailSize,
//                thumbnailSize / DIVIDER,
//                true
//        );
//        originalShadow.recycle();
//    }
//
//    private List<Bitmap> getParts(List<Album> albums) {
//        // Get all bitmaps
//        List<Bitmap> parts = getAlbumArts(albums);
//
//        while (parts.size() > MAX_ITEMS) {
//            parts.remove(0);
//        }
//
//        if (parts.size() != 0) {
//            initDividers(parts.size());
//            // Scale them down
//            List<Bitmap> scaledParts = scaleParts(parts);
//
//            return scaledParts;
//        }
//
//        return parts;
//    }
//
//    private void initDividers(int size) {
//        dividers = new int[size];
//
//        for (int i = 0; i < size; i++) {
//            dividers[size - i - 1] = (int) (DIVIDER * Math.pow(1.5, i));
//        }
//
//        dividers[size - 1] = 1;
//    }
//
//    private List<Bitmap> getAlbumArts(List<Album> albums) {
//        List<Bitmap> parts = new ArrayList<>();
//        for (Album album : albums) {
//            try {
//                parts.add(
//                        Glide.with(context)
//                                .load("file://" + album.getAlbumArtPath())
//                                .asBitmap()
//                                .get()
//                );
//            } catch (Exception ignore) {}
//        }
//        return parts;
//    }
//
//    private List<Bitmap> scaleParts(List<Bitmap> parts) {
//        List<Bitmap> scaledParts = new ArrayList<>();
//        for (Bitmap bitmap : parts) {
//            scaledParts.add(getScaledThumbnail(bitmap));
//        }
//        return scaledParts;
//    }
//
//    private Bitmap getScaledThumbnail(Bitmap bitmap) {
//        return ThumbnailUtils.extractThumbnail(
//                bitmap, thumbnailSize, thumbnailSize
//        );
//    }
//
//    private List<Bitmap> cropBackgroundParts(List<Bitmap> parts) {
//        List<Bitmap> newParts = new ArrayList<>();
//        for (int i = 0; i < parts.size(); i++) {
//            Bitmap part = parts.get(i);
//            newParts.add(Bitmap.createBitmap(part, 0, 0, thumbnailSize, getHeightForIndex(i)));
//            part.recycle();
//        }
//        return newParts;
//    }
//
//    public int getHeightForIndex(int i) {
//        int additional = dividers[i] == 1 ? 0 : 1;
//        return thumbnailSize / dividers[i] + additional;
//    }
//
//    public int getOffsetForIndex(int i) {
//        int offset = 0;
//        for (int j = 0; j < i; j++) {
//            offset += getHeightForIndex(j);
//        }
//        return offset;
//    }
//
//    private Bitmap getBackgroundPartShadowForHeight(int height) {
//        return Bitmap.createScaledBitmap(shadow, thumbnailSize, height, true);
//    }
//
//    private Bitmap overlapParts(List<Bitmap> parts, int height) {
//        Bitmap result = Bitmap.createBitmap(thumbnailSize, height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(result);
//        Paint paint = new Paint();
//        for (int i = 0; i < parts.size(); i++) {
//            int h = getHeightForIndex(i);
//            int offset = getOffsetForIndex(i);
//            canvas.drawBitmap(parts.get(i), 0, offset, paint);
//            if (i < parts.size() - 1) {
//                canvas.drawBitmap(getBackgroundPartShadowForHeight(h), 0, offset, paint);
//            }
//        }
//        return result;
//    }
//
//    private Bitmap createOverlappedBitmap(List<Bitmap> parts) {
//        parts = cropBackgroundParts(parts);
//
//        int totalHeight = 0;
//        for (int i = 0; i < parts.size(); i++) {
//            totalHeight += getHeightForIndex(i);
//        }
//
//        return overlapParts(parts, totalHeight);
//    }
//
//    public Bitmap getVerticalOverlappedAlbumsBitmap() {
//        List<Bitmap> parts = getParts(artist.getAlbums());
//        if (parts.size() == 1) {
//            return parts.get(0);
//        } else if (parts.size() == 0) {
//            return getScaledThumbnail(BitmapFactory.decodeResource(resources, R.drawable.no_cover));
//        }
//        return createOverlappedBitmap(parts);
//    }
//
//}
