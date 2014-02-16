package com.lwm.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.helper.SongsCursorGetter;

public class SongsCursorAdapterBackup extends BasicCursorAdapter {

    public SongsCursorAdapterBackup(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    static class ViewHolder {
        public TextView title;
        public TextView artist;
        public ImageView coverAlbum;
        public ProgressBar progress;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View rowView = inflater.inflate(R.layout.list_item_songs, null, false);

        ViewHolder holder = new ViewHolder();

        assert rowView != null;
        holder.title = (TextView) rowView.findViewById(R.id.songs_list_item_title);
        holder.artist = (TextView) rowView.findViewById(R.id.songs_list_item_artist);
//        holder.coverAlbum = (ImageView) rowView.findViewById(R.id.songs_list_item_album_cover);
//        holder.progress = (ProgressBar) rowView.findViewById(R.id.songs_list_item_album_cover_progress);
        rowView.setTag(holder);

        return rowView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.title.setText(cursor.getString(SongsCursorGetter.TITLE));
        holder.artist.setText(cursor.getString(SongsCursorGetter.ARTIST));

        // Using an AsyncTask to load the slow images in a background thread
        new AsyncTask<ViewHolder, Void, Bitmap>() {
            private ViewHolder v;
            private TextView title;

            @Override
            protected Bitmap doInBackground(ViewHolder... params) {
                v = params[0];
                title = v.title;
                return getAlbumart(cursor.getLong(SongsCursorGetter.ALBUM_ID));
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (v.title == title) {
                    // If this item hasn't been recycled already, hide the
                    // progress and set and show the image
                    v.progress.setVisibility(View.GONE);
                    v.coverAlbum.setVisibility(View.VISIBLE);

                    if(result!=null){
                        v.coverAlbum.setImageBitmap(result);
                    }else{
                        v.coverAlbum.setImageResource(R.drawable.ic_no_cover);
                    }


                }
            }
        }.execute(holder);

//        /* Get art work from getAlbumart */
//        Bitmap art=getAlbumart(cursor.getLong(SongsCursorGetter.ALBUM_ID));
//        if(art!=null)
//            holder.coverAlbum.setImageBitmap(art);
//        else
//            holder.coverAlbum.setImageResource(R.drawable.ic_no_cover);

    }

}