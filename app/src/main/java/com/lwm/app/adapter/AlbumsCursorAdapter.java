package com.lwm.app.adapter;

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

public class AlbumsCursorAdapter extends BasicCursorAdapter {

    private final Uri artworkUri = Uri
            .parse("content://media/external/audio/albumart");

    public AlbumsCursorAdapter(Context context, AlbumsCursorGetter c) {
        super(context, c.getAlbums());
        this.context = context;
    }

    static class ViewHolder {
        public TextView album;
        public TextView artist;
        public ImageView coverAlbum;
        public int position;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View rowView = inflater.inflate(R.layout.list_item_albums, null, false);

        ViewHolder holder = new ViewHolder();

        assert rowView != null;
        holder.album = (TextView) rowView.findViewById(R.id.albums_list_item_album);
        holder.artist = (TextView) rowView.findViewById(R.id.albums_list_item_artist);
        holder.coverAlbum = (ImageView) rowView.findViewById(R.id.albums_list_item_cover);
        rowView.setTag(holder);

        return rowView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.album.setText(cursor.getString(AlbumsCursorGetter.ALBUM));

        holder.artist.setText(cursor.getString(AlbumsCursorGetter.ARTIST));

        holder.coverAlbum.setImageDrawable(context.getResources().getDrawable(R.drawable.no_cover));

        holder.position = cursor.getPosition();

        new AlbumArtAsyncGetter(context, holder, holder.position).execute(
                ContentUris.withAppendedId(artworkUri, cursor.getLong(AlbumsCursorGetter._ID))
        );
//        Bitmap art=getAlbumart(cursor.getLong(AlbumsCursorGetter._ID));
//        if(art!=null)
//            holder.coverAlbum.setImageBitmap(art);
//        else
//            holder.coverAlbum.setImageResource(R.drawable.no_cover);
    }


    private static class AlbumArtAsyncGetter extends AsyncTask<Uri, Void, Bitmap> {
        private Context context;
        private ViewHolder holder;
        private int position;

        public AlbumArtAsyncGetter(Context context, ViewHolder holder, int position){
            this.context = context;
            this.holder = holder;
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(Uri... uri) {
            try {
                return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri[0]);
            } catch (IOException e) {
//                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (holder.position == position) {
                if(bitmap != null)   holder.coverAlbum.setImageBitmap(bitmap);
                else                 holder.coverAlbum.setImageResource(R.drawable.no_cover);
            }
        }
    }
}
