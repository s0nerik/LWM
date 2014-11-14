package com.lwm.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.koushikdutta.ion.Ion;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Album;
import com.lwm.app.ui.SingleBitmapPaletteInfoCallback;
import com.lwm.app.ui.custom_view.SquareWidthImageView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AlbumsAdapter extends ArrayAdapter<Album> {

    private final Context context;
    private List<Album> albumsList;

    @Inject
    Utils utils;

    public AlbumsAdapter(final Context context, List<Album> albums) {
        super(context, R.layout.list_item_songs, albums);
        Injector.inject(this);
        this.context = context;
        albumsList = albums;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.item_albums, null, true);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Album album = albumsList.get(position);
        holder.mTitle.setText(album.getTitle());
        holder.mSubtitle.setText(utils.getArtistName(album.getArtist()));

        Ion.with(holder.mCover)
                .smartSize(true)
                .placeholder(R.color.grid_item_default_bg)
                .error(R.drawable.no_cover)
                .load("file://" + album.getAlbumArtPath())
                .withBitmapInfo()
                .setCallback(new SingleBitmapPaletteInfoCallback(holder.mBottomBar, holder.mTitle, holder.mSubtitle));

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
        com.danh32.fontify.TextView mTitle;
        @InjectView(R.id.subtitle)
        com.danh32.fontify.TextView mSubtitle;
        @InjectView(R.id.bottom_bar)
        LinearLayout mBottomBar;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
