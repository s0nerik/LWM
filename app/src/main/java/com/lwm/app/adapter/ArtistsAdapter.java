package com.lwm.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Artist;
import com.lwm.app.model.ArtistsList;

import java.util.List;

import javax.inject.Inject;

public class ArtistsAdapter extends ArrayAdapter<Artist> {

    private final Context context;
    private List<Artist> artistsList;

    @Inject
    Utils utils;

    public ArtistsAdapter(final Context context, ArtistsList artists) {
        super(context, R.layout.list_item_songs, artists.getArtists());
        Injector.inject(this);
        this.context = context;
        artistsList = artists.getArtists();
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
        holder.artist.setText(utils.getArtistName(artist.getName()));
        holder.albums.setText("Albums: "+artist.getNumberOfAlbums());

        return rowView;
    }

}
