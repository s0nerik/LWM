package com.lwm.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.events.ui.ShouldStartArtistInfoActivity;
import com.lwm.app.model.Artist;
import com.lwm.app.model.ArtistWrapper;
import com.lwm.app.model.ArtistWrapperList;
import com.squareup.otto.Bus;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistWrappersAdapter.ViewHolder> {

    private List<ArtistWrapper> artistWrapperList;

    private final Context context;

    @Inject
    Bus bus;

    @Inject
    Utils utils;

    public ArtistWrappersAdapter(Context context, ArtistWrapperList artists) {
        Injector.inject(this);
        artistWrapperList = artists.getArtistWrappers();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(context, R.layout.item_artists, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        ArtistWrapper artistWrapper = artistWrapperList.get(i);
        Artist artist = artistWrapper.getArtist();
        String artistName = utils.getArtistName(artist.getName());
        holder.mTitle.setText(artistName);
        holder.mSubtitle.setText(artist.getNumberOfAlbums() + " albums, " + artist.getNumberOfSongs() + " songs");

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(WordUtils.capitalize(artistName.substring(0, 2)),
                        ColorGenerator.DEFAULT.getColor(artistName));

        holder.mImageView.setImageDrawable(drawable);

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
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.subtitle)
        TextView mSubtitle;
        @InjectView(R.id.imageView)
        ImageView mImageView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        @OnClick(R.id.itemLayout)
        public void onClick() {
            bus.post(new ShouldStartArtistInfoActivity(artistWrapperList.get(getPosition()).getArtist()));
        }

    }

}
