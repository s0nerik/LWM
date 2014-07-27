package com.lwm.app.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;

import java.util.List;

public class SongsListAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private List<Song> list;
    private Utils utils;

    private class OnContextButtonClickListener implements View.OnClickListener {
        private int position;

        private OnContextButtonClickListener(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View view) {
            PopupMenu menu = new PopupMenu(context, view);
            menu.inflate(R.menu.songs_popup);
            menu.setOnMenuItemClickListener(new OnContextMenuItemClickListener(position));
            menu.show();
        }

    }

    private class OnContextMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private int position;

        private OnContextMenuItemClickListener(int pos) {
            position = pos;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.action_add_to_queue:
                    LocalPlayerService player = App.getLocalPlayerService();
                    player.addToQueue(list.get(position));
                    Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
                    toast.show();
                    return true;
                default:
                    return false;
            }
        }
    }

    public SongsListAdapter(Context context, List<Song> list) {
        super(context, R.layout.list_item_songs, list);
        this.context = context;
        this.list = list;
        utils = new Utils(context);
    }

    static class ViewHolder {
        public TextView title;
        public TextView artist;
        public TextView duration;
        public ImageView contextMenu;
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
            holder.contextMenu = (ImageView) rowView.findViewById(R.id.button_context_menu);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Song song = list.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(utils.getArtistName(song.getArtist()));
        holder.duration.setText(song.getDurationString());

        holder.contextMenu.setOnClickListener(new OnContextButtonClickListener(position));

//        if(position == 0){
//            rowView.findViewById(R.id.space).setVisibility(View.VISIBLE);
//        }

        return rowView;
    }



}
