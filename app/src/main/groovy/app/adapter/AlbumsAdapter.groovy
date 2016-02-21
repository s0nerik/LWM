package app.adapter
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
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
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.util.Util
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class AlbumsAdapter extends ArrayAdapter<Album> {

    private final Context context
    private final List<Album> albums

    @Inject
    @PackageScope
    Utils utils

    @Inject
    @PackageScope
    LayoutInflater inflater

    private PaletteApplier paletteApplier = new PaletteApplier(0.75f)

    public AlbumsAdapter(final Context context, List<Album> albums) {
        super(context, R.layout.item_songs, albums as List<Album>)
        Injector.inject(this)
        this.context = context
        this.albums = albums
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AlbumViewHolder holder

        View rowView = convertView
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_albums, parent, false)
            holder = new AlbumViewHolder(rowView)
            rowView.setTag(holder)
        } else {
            holder = (AlbumViewHolder) rowView.getTag()
        }

        Album album = albums[position]
        holder.mTitle.text = album.title
        holder.mSubtitle.text = utils.getArtistName album.artistName
//        holder.mBottomBar.setBackgroundResource(R.color.grid_item_default_bg);

        String url = "file://${album.albumArtPath}"
        Glide.with(holder.mCover.context)
                .load(url)
                .centerCrop()
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .crossFade()
//                .listener(GlidePalette.with(url)
//                        .intoCallBack({ Palette palette ->
//                            applyPalette(palette,
//                                    context.resources,
//                                    holder.mTitle,
//                                    holder.mSubtitle,
//                                    holder.mBottomBar)
////                            new PaletteApplier(
////                                    resources: context.resources,
////                                    title: holder.mTitle,
////                                    subtitle: holder.mSubtitle,
////                                    layout: holder.mBottomBar
////                            ).apply(bitmap.palette)
//                        })
////                        .use(MUTED)
////                        .intoBackground(holder.mBottomBar, RGB)
////                        .intoTextColor(holder.mTitle, TITLE_TEXT_COLOR)
////                        .intoTextColor(holder.mSubtitle, BODY_TEXT_COLOR)
//                )
                .into(holder.mCover)

//        Glide.with(holder.mCover.context)
//                .load("file://" + album.getAlbumArtPath())
//                .asBitmap()
//                .transcode(new PaletteBitmapTranscoder(holder.mCover.context), PaletteBitmap)
//                .centerCrop()
//                .error(R.drawable.no_cover)
//                .placeholder(R.color.grid_item_default_bg)
//                .into(new PaletteBitmapImageViewTarget(holder.mCover) {
//                    @Override
//                    public void onResourceReady(PaletteBitmap bitmap, GlideAnimation anim) {
//                        setResource(bitmap)
////                            applyPalette(bitmap.palette,
////                                    context.resources,
////                                    holder.mTitle,
////                                    holder.mSubtitle,
////                                    holder.mBottomBar)
//
////                        new PaletteApplier(
////                                resources: context.resources,
////                                bitmap: bitmap.bitmap,
////                                title: holder.mTitle,
////                                subtitle: holder.mSubtitle,
////                                layout: holder.mBottomBar
////                        ).apply(bitmap.palette)
////                        MaterialImageLoading.animate(holder.mCover).setDuration(2000).start()
//                    }
//                    @Override
//                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
////                        applyPalette(null,
////                                context.resources,
////                                holder.mTitle,
////                                holder.mSubtitle,
////                                holder.mBottomBar)
//
////                        new PaletteApplier(
////                                resources: context.resources,
////                                title: holder.mTitle,
////                                subtitle: holder.mSubtitle,
////                                layout: holder.mBottomBar
////                        ).apply(null)
//                        holder.mCover.imageDrawable = errorDrawable
////                        MaterialImageLoading.animate(holder.mCover).setDuration(2000).start()
//                    }
//                })

        return rowView
    }

    private void applyPalette(Palette palette, Resources resources, TextView title, TextView subtitle, View layout) {
        paletteApplier.resources = resources
        paletteApplier.title = title
        paletteApplier.subtitle = subtitle
//        paletteApplier.layout = layout
        paletteApplier.apply(palette)
    }

    private static class PaletteBitmap {
        private final Bitmap mBitmap
        private final Palette mPalette;

        public PaletteBitmap(Bitmap bitmap, Palette palette) {
            mBitmap = bitmap
            mPalette = palette
        }

        public Bitmap getBitmap(){
            return mBitmap
        }

        public Palette getPalette(){
            return mPalette
        }
    }



    private static class PaletteBitmapResource implements Resource<PaletteBitmap> {

        private final PaletteBitmap mPaletteBitmap
        private final BitmapPool mBitmapPool;

        public PaletteBitmapResource(PaletteBitmap bitmap, BitmapPool bitmapPool) {
            mPaletteBitmap = bitmap
            mBitmapPool = bitmapPool
        }

        @Override
        public PaletteBitmap get() {
            return mPaletteBitmap
        }

        @Override
        public int getSize() {
            return Util.getBitmapByteSize(mPaletteBitmap.getBitmap())
        }

        @Override
        public void recycle() {
            if (mPaletteBitmap != null && mPaletteBitmap.getBitmap() != null) {
                mBitmapPool.put(mPaletteBitmap.getBitmap())
            }
        }
    }

    private static class PaletteBitmapImageViewTarget extends ImageViewTarget<PaletteBitmap> {
        public PaletteBitmapImageViewTarget(ImageView view) {
            super(view)
        }

        @Override
        protected void setResource(PaletteBitmap resource) {
            view.setImageBitmap(resource.getBitmap())
        }
    }

    private static class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmap> {

        private final BitmapPool mBitmapPool;

        public PaletteBitmapTranscoder(Context context) {
            this(Glide.get(context).getBitmapPool())
        }

        public PaletteBitmapTranscoder(BitmapPool bitmapPool) {
            mBitmapPool = bitmapPool
        }

        @Override
        public Resource<PaletteBitmap> transcode(Resource<Bitmap> toTranscode) {
            PaletteBitmap result = new PaletteBitmap(toTranscode.get(), Palette.from(toTranscode.get()).generate())
            return new PaletteBitmapResource(result, mBitmapPool)
        }

        @Override
        public String getId() {
            return ""
        }
    }

}
