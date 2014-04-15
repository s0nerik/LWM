package com.lwm.app.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.model.Album;
import com.lwm.app.model.Artist;
import com.lwm.app.model.ArtistsList;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

public class ArtistsAdapter extends ArrayAdapter<Artist> {

    private final Context context;
    private List<Artist> artistsList;
    private static ContentResolver contentResolver;

    public ArtistsAdapter(final Context context, ArtistsList artists) {
        super(context, R.layout.list_item_songs, artists.getArtists());
        this.context = context;
        artistsList = artists.getArtists();
        contentResolver = context.getContentResolver();
    }

    static class ViewHolder {
        public TextView artist;
        public TextView albums;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.list_item_artists, null, true);
            holder = new ViewHolder();

            holder.artist = (TextView) rowView.findViewById(R.id.artists_list_item_artist);
            holder.albums = (TextView) rowView.findViewById(R.id.artists_list_item_albums_count);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Artist artist = artistsList.get(position);
        holder.artist.setText(artist.getName());
        holder.albums.setText(artist.getNumberOfAlbums());

        return rowView;
    }

}
