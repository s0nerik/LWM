package com.lwm.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistWrappersAdapter.ViewHolder> {

    private static final int MAX_SIZE = 6;

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
    private int coverSize;

    public ArtistWrappersAdapter(ArtistWrapperList artists) {
        Injector.inject(this);
        artistWrapperList = artists.getArtistWrappers();
        displayWidth = utils.getScreenWidth();
        coverSize = resources.getDimensionPixelSize(R.dimen.item_covers_size);
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
//        File f;
        for (Album a : albums) {
//            f = new File(a.getAlbumArtPath());
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

        new AlbumsBitmapLoaderTask(
                holder.mCovers,
                displayWidth / albums.size(),
                coverSize,
                albums
        ).execute();

//        holder.mRecyclerView.setLayoutManager(
//                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
//        holder.mRecyclerView.setHasFixedSize(true);
//        holder.mRecyclerView.setAdapter(
//                new AlbumCoversAdapter(
//                        albums,
//                        holder.mRecyclerView / albums.size(),
//                        holder.mTextLayout,
//                        holder.mTitle,
//                        holder.mSubtitle
//                )
//        );

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
        @InjectView(R.id.covers)
        ImageView mCovers;
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

    class AlbumsBitmapLoaderTask extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<ImageView> ref;

        private final ImageView imageView;
        private final int albumWidth;
        private final int albumHeight;
        private final List<Album> albums;

        public AlbumsBitmapLoaderTask(ImageView imageView, int albumWidth, int albumHeight, List<Album> albums) {
            this.imageView = imageView;
            this.albumWidth = albumWidth;
            this.albumHeight = albumHeight;
            this.albums = albums;

            this.ref = new WeakReference<>(imageView);
        }

        @Override
        protected void onPreExecute() {
            imageView.setImageResource(android.R.color.black);
        }

        @Override
        protected Bitmap doInBackground(Void... args) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap[] images = new Bitmap[albums.size()];
            Bitmap tmp = null;
            Bitmap result = null;

            try {
                int j = 0;
                for (Album a : albums) {
//                    File f = new File(a.getAlbumArtPath());
                    tmp = BitmapFactory.decodeFile(a.getAlbumArtPath(), options);
//                    tmp = Ion.with(context).load("file://" + a.getAlbumArtPath()).asBitmap().get(1, TimeUnit.SECONDS);
                    tmp = ThumbnailUtils.extractThumbnail(tmp, albumWidth, albumHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    images[j++] = tmp;
                    tmp = null;
                }

                result = Bitmap.createBitmap(albumWidth * albums.size(), albumHeight, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(result);

                canvas.drawBitmap(images[0], 0, 0, null);
                int currentWidth = images[0].getWidth();
                images[0].recycle();
                images[0] = null;
                for (int i = 1; i < images.length; i++) {
                    canvas.drawBitmap(images[i], currentWidth, 0, null);
                    currentWidth += images[i].getWidth();
                    images[i].recycle();
                    images[i] = null;
                }

                return result;
            } catch (Exception e) {
                for (Bitmap b : images) {
                    if (b != null) b.recycle();
                }
                if (tmp != null) {
                    tmp.recycle();
                }
                if (result != null) {
                    result.recycle();
                }

//                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (ref != null) {
                ImageView imageView = ref.get();
                if (imageView != null) {

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageDrawable(imageView.getContext().getResources()
                                .getDrawable(R.drawable.no_cover));
                    }
                }

            }

        }

        public void loadBitmap(int resId, ImageView imageView) {
            if (cancelPotentialWork(resId, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(resId);
            }
        }

    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<AlbumsBitmapLoaderTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, AlbumsBitmapLoaderTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public AlbumsBitmapLoaderTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

}
