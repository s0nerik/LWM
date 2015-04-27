package app.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import app.Injector;
import com.lwm.app.R;
import app.model.Album;
import app.ui.SingleBitmapPaletteInfoCallback;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AlbumCoversAdapter extends RecyclerView.Adapter<AlbumCoversAdapter.ViewHolder> {

    @Inject
    LayoutInflater inflater;

    @Inject
    Resources resources;

    private List<Album> albums;

    private int coverSize;

    private int width;

    private View textLayout;
    private TextView title;
    private TextView subtitle;

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
    public AlbumCoversAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.item_covers, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final AlbumCoversAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.mImage.setMaxWidth(width);
        viewHolder.mImage.setMinimumWidth(width);
        Ion.with(viewHolder.mImage)
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .transform(new CropTransform())
                .load("file://" + albums.get(i).getAlbumArtPath())
                .withBitmapInfo()
                .setCallback(new BitmapPaletteInfoCallback(i, viewHolder));
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
            ButterKnife.inject(this, view);
        }
    }

    private class CropTransform implements Transform {
        @Override
        public Bitmap transform(Bitmap b) {
            Bitmap thumb = ThumbnailUtils.extractThumbnail(
                    b, coverSize, coverSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

            Bitmap x;
            if (width <= coverSize) {
                x = android.graphics.Bitmap.createBitmap(thumb, 0, 0, width, coverSize);
            } else {
                x = android.graphics.Bitmap.createBitmap(thumb, 0, 0, coverSize, coverSize);
            }
            thumb.recycle();
            return x;
        }

        @Override
        public String key() {
            return null;
        }
    }

    public class BitmapPaletteInfoCallback extends SingleBitmapPaletteInfoCallback {

        private int i;
        private ViewHolder holder;

        private BitmapPaletteInfoCallback(int i, ViewHolder holder) {
            super(textLayout, title, subtitle);
            this.i = i;
            this.holder = holder;
        }

        @Override
        public void onCompleted(Exception e, ImageViewBitmapInfo result) {
            if (i < albums.size() - 1) {
                holder.mShadow.setVisibility(View.VISIBLE);
            } else {
//                super.onCompleted(e, result);
            }
        }
    }

}
