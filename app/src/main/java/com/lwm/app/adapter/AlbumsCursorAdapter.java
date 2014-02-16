/*
package com.lwm.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.helper.AlbumsCursorGetter;

public class AlbumsCursorAdapter extends BasicCursorAdapter {

    public AlbumsCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    static class ViewHolder {
        public TextView album;
        public TextView artist;
        public ImageView coverAlbum;
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

        */
/* Get art work from getAlbumart *//*

        Bitmap art=getAlbumart(cursor.getLong(AlbumsCursorGetter._ID));
        if(art!=null)
            holder.coverAlbum.setImageBitmap(art);
        else
            holder.coverAlbum.setImageResource(R.drawable.ic_no_cover);
    }

}*/
