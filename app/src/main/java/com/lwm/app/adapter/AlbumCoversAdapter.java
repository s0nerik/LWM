package com.lwm.app.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.model.Album;

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

    public AlbumCoversAdapter(List<Album> albums) {
        Injector.inject(this);
        this.albums = albums;
        coverSize = resources.getDimensionPixelSize(R.dimen.item_covers_size);
    }

    @Override
    public AlbumCoversAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.item_covers, null, true));
    }

    @Override
    public void onBindViewHolder(AlbumCoversAdapter.ViewHolder viewHolder, int i) {
        Ion.with(viewHolder.mImage)
                .error(R.drawable.no_cover)
                .placeholder(R.drawable.no_cover)
                .resize(coverSize, coverSize)
                .load("file://" + albums.get(i).getAlbumArtPath());
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

        ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
