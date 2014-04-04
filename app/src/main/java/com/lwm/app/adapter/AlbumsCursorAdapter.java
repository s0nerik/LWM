package com.lwm.app.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.helper.AlbumsCursorGetter;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class AlbumsCursorAdapter extends BasicCursorAdapter {

    private static final Uri artworkUri = Uri
            .parse("content://media/external/audio/albumart");

    private static ContentResolver contentResolver;

    public AlbumsCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
        contentResolver = context.getContentResolver();
    }

    private TextView album;
    private TextView artist;
    private ImageView coverAlbum;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View rowView = inflater.inflate(R.layout.list_item_albums, null, false);

        assert rowView != null;
        album = (TextView) rowView.findViewById(R.id.albums_list_item_album);
        artist = (TextView) rowView.findViewById(R.id.albums_list_item_artist);
        coverAlbum = (ImageView) rowView.findViewById(R.id.albums_list_item_cover);

        return rowView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        album.setText(cursor.getString(AlbumsCursorGetter.ALBUM));

        artist.setText(cursor.getString(AlbumsCursorGetter.ARTIST));

        new AlbumArtThumbnailer(coverAlbum).execute(
                ContentUris.withAppendedId(artworkUri, cursor.getLong(AlbumsCursorGetter._ID))
        );
//        Bitmap art=getAlbumart(cursor.getLong(AlbumsCursorGetter._ID));
//        if(art!=null)
//            holder.coverAlbum.setImageBitmap(art);
//        else
//            holder.coverAlbum.setImageResource(R.drawable.no_cover);
    }

    private static class AlbumArtThumbnailer extends AsyncTask<Uri, Void, Bitmap> {
        private WeakReference<ImageView> imageViewSoftReference;
        private int mPosition;

        public AlbumArtThumbnailer(ImageView imageView){
            mPosition = imageView.getId();
            imageViewSoftReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Uri... uri) {
            try {
                return MediaStore.Images.Media.getBitmap(contentResolver, uri[0]);
            } catch (IOException e) {
//                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imageViewSoftReference != null
                    && imageViewSoftReference.get() != null
                    && mPosition == imageViewSoftReference.get().getId())
                imageViewSoftReference.get().setImageBitmap(bitmap);
//            if (holder.position == position) {
//                if(bitmap != null)   holder.coverAlbum.setImageBitmap(bitmap);
//                else                 holder.coverAlbum.setImageResource(R.drawable.no_cover);
//            }
        }
    }
}
