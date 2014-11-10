package com.lwm.app.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danh32.fontify.TextView;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Album;
import com.lwm.app.model.Artist;
import com.lwm.app.model.ArtistWrapper;
import com.lwm.app.model.ArtistWrapperList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistWrappersAdapter.ViewHolder> {

    private List<ArtistWrapper> artistWrapperList;

    @Inject
    Utils utils;

    @Inject
    LayoutInflater inflater;

    @Inject
    Context context;

    public ArtistWrappersAdapter(ArtistWrapperList artists) {
        Injector.inject(this);
        artistWrapperList = artists.getArtistWrappers();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.item_artists, null, true));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        ArtistWrapper artistWrapper = artistWrapperList.get(i);
        Artist artist = artistWrapper.getArtist();
        holder.mTitle.setText(utils.getArtistName(artist.getName()));
        holder.mSubtitle.setText("Albums: " + artist.getNumberOfAlbums());

        List<Album> albums = artistWrapper.getAlbums();

        List<Album> blacklist = new ArrayList<>();
        for (Album a : albums) {
            if (a.getAlbumArtPath() == null) {
                blacklist.add(a);
            }
        }
        albums.removeAll(blacklist);

        holder.mRecyclerView.setHasFixedSize(true);
        holder.mRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.mRecyclerView.setAdapter(new AlbumCoversAdapter(albums));

    }

    @Override
    public int getItemCount() {
        return artistWrapperList.size();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_artists.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.recyclerView)
        RecyclerView mRecyclerView;
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.subtitle)
        TextView mSubtitle;

        ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
