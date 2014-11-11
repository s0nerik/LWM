package com.lwm.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danh32.fontify.TextView;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.events.ui.ShouldStartArtistInfoActivity;
import com.lwm.app.model.Album;
import com.lwm.app.model.Artist;
import com.lwm.app.model.ArtistWrapper;
import com.lwm.app.model.ArtistWrapperList;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistWrappersAdapter.ViewHolder> {

    private static final int MAX_SIZE = 8;

    private List<ArtistWrapper> artistWrapperList;

    @Inject
    Bus bus;

    @Inject
    Utils utils;

    @Inject
    LayoutInflater inflater;

    @Inject
    Context context;

    @Inject
    Resources resources;

    private int displayWidth;

    public ArtistWrappersAdapter(ArtistWrapperList artists) {
        Injector.inject(this);
        artistWrapperList = artists.getArtistWrappers();
        displayWidth = utils.getScreenWidth();
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

        if (albums.size() == 0) {
            albums.add(new Album(null));
        } else if (albums.size() > MAX_SIZE) {
            albums = albums.subList(albums.size() - MAX_SIZE, albums.size());
        }

        holder.mRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.mRecyclerView.setAdapter(
                new AlbumCoversAdapter(
                        albums,
                        displayWidth / albums.size(),
                        holder.mTextLayout,
                        holder.mTitle,
                        holder.mSubtitle
                )
        );

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
    class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.recyclerView)
        RecyclerView mRecyclerView;
        @InjectView(R.id.text_layout)
        View mTextLayout;
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.subtitle)
        TextView mSubtitle;

        ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        @OnClick(R.id.touch_area)
        public void onClick() {
            bus.post(new ShouldStartArtistInfoActivity(artistWrapperList.get(getPosition()).getArtist()));
        }
    }
}
