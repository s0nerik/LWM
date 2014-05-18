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
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.service.MusicServerService;
import com.lwm.app.service.MusicService;

import java.util.List;

public class SongsListAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private List<Song> list;

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
                    MusicService musicService = App.getMusicService();
                    LocalPlayer player = musicService.getLocalPlayer();
                    if(player == null){
                        player = new LocalPlayer(context);
                        musicService.setLocalPlayer(player);
                    }
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
        holder.artist.setText(song.getArtist());
        holder.duration.setText(song.getDurationString());

        // A very bad solution, but I don't see any other way how to do it.
        holder.contextMenu.setOnClickListener(new OnContextButtonClickListener(position));

        return rowView;
    }



}
