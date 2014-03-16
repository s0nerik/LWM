package com.lwm.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private List<Song> songsList;

    public PlaylistAdapter(Context context, Playlist playlist) {
        super(context, R.layout.list_item_songs, playlist.getSongs());
        this.context = context;
        songsList = playlist.getSongs();
    }

    static class ViewHolder {
        public TextView title;
        public TextView artist;
        public TextView duration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.list_item_songs, null, true);
            holder = new ViewHolder();

            holder.title = (TextView) rowView.findViewById(R.id.songs_list_item_title);
            holder.artist = (TextView) rowView.findViewById(R.id.songs_list_item_artist);
            holder.duration = (TextView) rowView.findViewById(R.id.songs_list_item_duration);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Song song = songsList.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.duration.setText(song.getDurationString());

        return rowView;
    }

}
