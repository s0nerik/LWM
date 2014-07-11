package com.lwm.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Album;
import com.lwm.app.model.AlbumsList;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlbumsAdapter extends ArrayAdapter<Album> {

    private static final String artworkUri = "content://media/external/audio/albumart/";

    private final Context context;
    private Resources resources;
    private List<Album> albumsList;

    public AlbumsAdapter(final Context context, AlbumsList albums) {
        super(context, R.layout.list_item_songs, albums.getAlbums());
        this.context = context;
        resources = context.getResources();
        albumsList = albums.getAlbums();
    }

    static class ViewHolder {
        public TextView album;
        public TextView artist;
        public ImageView albumArt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.list_item_albums, null, true);
            holder = new ViewHolder();

            holder.album = (TextView) rowView.findViewById(R.id.albums_list_item_album);
            holder.artist = (TextView) rowView.findViewById(R.id.albums_list_item_artist);
            holder.albumArt = (ImageView) rowView.findViewById(R.id.albums_list_item_cover);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Album album = albumsList.get(position);
        holder.album.setText(album.getTitle());
        holder.artist.setText(Utils.getArtistName(album.getArtist()));

        Picasso.with(context)
                .load(artworkUri+album.getId())
                .resize(214, 214)
                .placeholder(R.drawable.no_cover)
                .centerCrop()
                .into(holder.albumArt);

        return rowView;
    }

}
