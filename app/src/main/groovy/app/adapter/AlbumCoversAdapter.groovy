package app.adapter
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.Injector
import app.R
import app.model.Album
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.bumptech.glide.Glide
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class AlbumCoversAdapter extends RecyclerView.Adapter<ViewHolder> {

    @Inject
    LayoutInflater inflater;

    @Inject
    Resources resources;

    List<Album> albums;

    private int coverSize;

    private int width;

    View textLayout;
    TextView title;
    TextView subtitle;

    public AlbumCoversAdapter(List<Album> albums, int width, View textLayout, TextView title, TextView subtitle) {
        Injector.inject(this);
        this.albums = albums;
        this.width = width;
        this.textLayout = textLayout;
        this.title = title;
        this.subtitle = subtitle;
        coverSize = resources.getDimensionPixelSize(R.dimen.item_covers_size);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.item_covers, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        viewHolder.mImage.setMaxWidth(width);
        viewHolder.mImage.setMinimumWidth(width);
        Glide.with(viewHolder.mImage.context)
                .load("file://" + albums.get(i).getAlbumArtPath())
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .centerCrop()
                .into(viewHolder.mImage)
        // TODO: generate palette
//                .withBitmapInfo()
//                .setCallback(new BitmapPaletteInfoCallback(i, viewHolder, textLayout, albums, title, subtitle));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_covers.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.image)
        ImageView mImage;
        @InjectView(R.id.layout)
        View mLayout;
        @InjectView(R.id.shadow)
        View mShadow;

        ViewHolder(View view) {
            super(view);
            SwissKnife.inject(this, view);
        }
    }

//    private class CropTransform implements Transform {
//        @Override
//        public Bitmap transform(Bitmap b) {
//            Bitmap thumb = ThumbnailUtils.extractThumbnail(
//                    b, coverSize, coverSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//
//            Bitmap x;
//            if (width <= coverSize) {
//                x = Bitmap.createBitmap(thumb, 0, 0, width, coverSize);
//            } else {
//                x = Bitmap.createBitmap(thumb, 0, 0, coverSize, coverSize);
//            }
//            thumb.recycle();
//            return x;
//        }
//
//        @Override
//        public String key() {
//            return null;
//        }
//    }

//    static class BitmapPaletteInfoCallback extends SingleBitmapPaletteInfoCallback {
//
//        private int i;
//        private ViewHolder holder;
//        private List<Album> albums
//
//        private BitmapPaletteInfoCallback(int i, ViewHolder holder, View textLayout, List<Album> albums, TextView title, TextView subtitle) {
//            super(textLayout, title, subtitle)
//            this.i = i
//            this.holder = holder
//            this.albums = albums
//        }
//
//        @Override
//        public void onCompleted(Exception e, ImageViewBitmapInfo result) {
//            if (i < albums.size() - 1) {
//                holder.mShadow.setVisibility(View.VISIBLE);
//            } else {
////                super.onCompleted(e, result);
//            }
//        }
//    }

}
