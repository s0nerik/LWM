package app.adapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import app.Injector
import app.R
import app.Utils
import app.adapter.view_holders.AlbumViewHolder
import app.model.Album
import app.ui.PaletteApplier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.util.Util
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class AlbumsAdapter extends ArrayAdapter<Album> {

    private final Context context;
    private List<Album> albumsList;

    @Inject
    Utils utils;

    @Inject
    LayoutInflater inflater;

    public AlbumsAdapter(final Context context, List<Album> albums) {
        super(context, R.layout.item_songs, albums);
        Injector.inject(this);
        this.context = context;
        albumsList = albums;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AlbumViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_albums, parent, false);
            holder = new AlbumViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (AlbumViewHolder) rowView.getTag();
        }

        Album album = albumsList.get(position);
        holder.mTitle.setText(album.getTitle());
        holder.mSubtitle.setText(utils.getArtistName(album.getArtist()));
        holder.mBottomBar.setBackgroundResource(R.color.grid_item_default_bg);

        Glide.with(holder.mCover.context)
                .load("file://" + album.getAlbumArtPath())
                .asBitmap()
                .transcode(new PaletteBitmapTranscoder(holder.mCover.context), PaletteBitmap.class)
                .centerCrop()
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .into(new PaletteBitmapImageViewTarget(holder.mCover) {
                    @Override
                    public void onResourceReady(PaletteBitmap bitmap, GlideAnimation anim) {
                        setResource(bitmap)
                        new PaletteApplier(
                                resources: context.resources,
                                bitmap: bitmap.bitmap,
                                title: holder.mTitle,
                                subtitle: holder.mSubtitle,
                                layout: holder.mBottomBar
                        ).apply(bitmap.palette)
//                        MaterialImageLoading.animate(holder.mCover).setDuration(2000).start()
                    }
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        new PaletteApplier(
                                resources: context.resources,
                                title: holder.mTitle,
                                subtitle: holder.mSubtitle,
                                layout: holder.mBottomBar
                        ).apply(null)
                        holder.mCover.imageDrawable = errorDrawable
//                        MaterialImageLoading.animate(holder.mCover).setDuration(2000).start()
                    }
                })

        return rowView;
    }

    private static class PaletteBitmap {
        private final Bitmap mBitmap;
        private final Palette mPalette;

        public PaletteBitmap(Bitmap bitmap, Palette palette) {
            mBitmap = bitmap;
            mPalette = palette;
        }

        public Bitmap getBitmap(){
            return mBitmap;
        }

        public Palette getPalette(){
            return mPalette;
        }
    }



    private static class PaletteBitmapResource implements Resource<PaletteBitmap> {

        private final PaletteBitmap mPaletteBitmap;
        private final BitmapPool mBitmapPool;

        public PaletteBitmapResource(PaletteBitmap bitmap, BitmapPool bitmapPool) {
            mPaletteBitmap = bitmap;
            mBitmapPool = bitmapPool;
        }

        @Override
        public PaletteBitmap get() {
            return mPaletteBitmap;
        }

        @Override
        public int getSize() {
            return Util.getBitmapByteSize(mPaletteBitmap.getBitmap());
        }

        @Override
        public void recycle() {
            if (mPaletteBitmap != null && mPaletteBitmap.getBitmap() != null) {
                mBitmapPool.put(mPaletteBitmap.getBitmap());
            }
        }
    }

    private static class PaletteBitmapImageViewTarget extends ImageViewTarget<PaletteBitmap> {
        public PaletteBitmapImageViewTarget(ImageView view) {
            super(view);
        }

        @Override
        protected void setResource(PaletteBitmap resource) {
            view.setImageBitmap(resource.getBitmap());
        }
    }

    private static class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmap> {

        private final BitmapPool mBitmapPool;

        public PaletteBitmapTranscoder(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public PaletteBitmapTranscoder(BitmapPool bitmapPool) {
            mBitmapPool = bitmapPool;
        }

        @Override
        public Resource<PaletteBitmap> transcode(Resource<Bitmap> toTranscode) {
            PaletteBitmap result = new PaletteBitmap(toTranscode.get(), Palette.from(toTranscode.get()).generate());
            return new PaletteBitmapResource(result, mBitmapPool);
        }

        @Override
        public String getId() {
            return "";
        }
    }

}
