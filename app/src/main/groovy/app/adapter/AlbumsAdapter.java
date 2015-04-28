package app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arasthel.swissknife.SwissKnife;
import com.arasthel.swissknife.annotations.InjectView;
import com.koushikdutta.ion.Ion;
import app.R;

import java.util.List;

import javax.inject.Inject;

import app.Injector;
import app.Utils;
import app.model.Album;
import app.ui.SingleBitmapPaletteInfoCallback;
import app.ui.custom_view.SquareWidthImageView;

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

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_albums, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Album album = albumsList.get(position);
        holder.mTitle.setText(album.getTitle());
        holder.mSubtitle.setText(utils.getArtistName(album.getArtist()));
        holder.mBottomBar.setBackgroundResource(R.color.grid_item_default_bg);

        Ion.with(holder.mCover)
                .smartSize(true)
                .placeholder(R.color.grid_item_default_bg)
                .error(R.drawable.no_cover)
                .load("file://" + album.getAlbumArtPath())
                .withBitmapInfo()
                .setCallback(new SingleBitmapPaletteInfoCallback(holder.mBottomBar, holder.mShadow, holder.mTitle, holder.mSubtitle));

        return rowView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_albums.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder {
        @InjectView(R.id.cover)
        SquareWidthImageView mCover;
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.subtitle)
        TextView mSubtitle;
        @InjectView(R.id.bottom_bar)
        LinearLayout mBottomBar;
        @InjectView(R.id.shadow)
        View mShadow;

        ViewHolder(View view) {
            SwissKnife.inject(this, view);
        }
    }
}
