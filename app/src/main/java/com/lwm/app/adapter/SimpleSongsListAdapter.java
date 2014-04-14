package com.lwm.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.model.Song;

import java.util.List;

public class SimpleSongsListAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private List<Song> songsList;

    public SimpleSongsListAdapter(Context context, List<Song> playlist) {
        super(context, R.layout.list_item_songs, playlist);
        this.context = context;
        songsList = playlist;
    }

    static class ViewHolder {
        public TextView title;
        public TextView duration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.list_item_songs_simple, null, true);
            holder = new ViewHolder();

            holder.title = (TextView) rowView.findViewById(R.id.songs_list_item_title);
            holder.duration = (TextView) rowView.findViewById(R.id.songs_list_item_duration);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Song song = songsList.get(position);
        holder.title.setText(song.getTitle());
        holder.duration.setText(song.getDurationString());

        return rowView;
    }

}
