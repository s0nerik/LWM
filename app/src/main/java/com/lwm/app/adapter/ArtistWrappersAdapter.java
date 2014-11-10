package com.lwm.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.danh32.fontify.TextView;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.lwm.app.App;
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
import butterknife.InjectViews;

public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistWrappersAdapter.ViewHolder> {

    private List<ArtistWrapper> artistsList;

    @Inject
    Utils utils;

    @Inject
    LayoutInflater inflater;

    @Inject
    Context context;

    public ArtistWrappersAdapter(ArtistWrapperList artists) {
        Injector.inject(this);
        artistsList = artists.getArtistWrappers();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.item_artists, null, true));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Artist artist = artistsList.get(i).getArtist();
        holder.mTitle.setText(utils.getArtistName(artist.getName()));
        holder.mSubtitle.setText("Albums: " + artist.getNumberOfAlbums());

        List<Album> albums = artist.getAlbums();

        List<Album> blacklist = new ArrayList<>();
        for (Album a : albums) {
            if (a.getAlbumArtPath() == null) {
                blacklist.add(a);
            }
        }
        albums.removeAll(blacklist);

        int albumsCount = albums.size();
        if (albumsCount >= 4) {
            setCoversGone(holder, 3, 3);

            Ion.with(holder.mImage)
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(0).getAlbumArtPath());

            Ion.with(holder.mBgImages.get(0))
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(1).getAlbumArtPath());

            Ion.with(holder.mBgImages.get(1))
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .transform(new Transform() {
                        @Override
                        public Bitmap transform(Bitmap b) {
                            return b;
                        }

                        @Override
                        public String key() {
                            return null;
                        }
                    })
                    .load("file://" + albums.get(2).getAlbumArtPath())
            .withBitmapInfo()
            .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                @Override
                public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                    final ImageView imageView = result.getImageView();
                    Bitmap res = result.getBitmapInfo().bitmaps[0];
                    res.getWidth();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(App.TAG, String.valueOf(imageView.getWidth()));
                        }
                    }, 2000);
                }
            })
            ;

            Ion.with(holder.mBgImages.get(2))
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(3).getAlbumArtPath());

        } else if (albumsCount == 3) {
            setCoversGone(holder, 2, 3);

            Ion.with(holder.mImage)
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(0).getAlbumArtPath());

            Ion.with(holder.mBgImages.get(0))
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(1).getAlbumArtPath());

            Ion.with(holder.mBgImages.get(1))
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(2).getAlbumArtPath());


        } else if (albumsCount == 2) {
            setCoversGone(holder, 1, 3);

            Ion.with(holder.mImage)
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(0).getAlbumArtPath());

            Ion.with(holder.mBgImages.get(0))
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(1).getAlbumArtPath());

        } else if (albumsCount == 1) {
            setCoversGone(holder, 0, 3);

            Ion.with(holder.mImage)
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load("file://"+albums.get(0).getAlbumArtPath());

        } else {
            setCoversGone(holder, 0, 3);

            Ion.with(holder.mImage)
                    .placeholder(R.drawable.no_cover)
                    .error(R.drawable.no_cover)
                    .smartSize(true)
                    .load(null);

        }

    }

    private void setCoversGone(ViewHolder holder, int from, int to) {
        ButterKnife.apply(holder.mBgLayouts.subList(from, to), GONE);
        ButterKnife.apply(holder.mBgLayouts.subList(0, from), VISIBLE);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    private static final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {
        @Override
        public void apply(View view, int index) {
            view.setVisibility(View.GONE);
        }
    };

    private static final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
        @Override
        public void apply(View view, int index) {
            view.setVisibility(View.VISIBLE);
        }
    };

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_artists.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectViews({R.id.bg_layout1, R.id.bg_layout2, R.id.bg_layout3})
        List<View> mBgLayouts;
        @InjectViews({R.id.bg_image1, R.id.bg_image2, R.id.bg_image3})
        List<ImageView> mBgImages;
        @InjectView(R.id.image)
        ImageView mImage;
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
