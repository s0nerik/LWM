package com.lwm.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.helper.ArtistsCursorGetter;

public class ArtistsCursorAdapter extends CursorAdapter {

    Context context;

    public ArtistsCursorAdapter(Context context, ArtistsCursorGetter c) {
        super(context, c.getArtists());
        this.context = context;
    }

    static class ViewHolder {
        public TextView artist;
        public TextView albumsNumber;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View rowView = inflater.inflate(R.layout.list_item_artists, null, false);

        ViewHolder holder = new ViewHolder();
        assert rowView != null;
        holder.artist = (TextView) rowView.findViewById(R.id.artists_list_item_artist);
        holder.albumsNumber = (TextView) rowView.findViewById(R.id.artists_list_item_albums_count);
        rowView.setTag(holder);

        return rowView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.artist.setText(cursor.getString(ArtistsCursorGetter.ARTIST));
        holder.albumsNumber.setText(
                context.getResources().getString(R.string.artists_list_item_caption_albums)+" "
                +cursor.getString(ArtistsCursorGetter.NUMBER_OF_ALBUMS)
        );
    }

}