package com.lwm.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;

import java.util.List;

public class SongsListAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private List<Song> list;
    private Utils utils;

    private LocalPlayer player;

    private class OnContextButtonClickListener implements View.OnClickListener {
        private int position;

        private OnContextButtonClickListener(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View view) {
            PopupMenu menu = new PopupMenu(context, view);

            if (player.isSongInQueue(list.get(position))) {
                menu.inflate(R.menu.songs_popup_in_queue);
            } else {
                menu.inflate(R.menu.songs_popup);
            }

            menu.setOnMenuItemClickListener(new OnContextMenuItemClickListener(position));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                final ImageView v = (ImageView) view;
                final ColorFilter oldFilter = v.getColorFilter();
                v.setColorFilter(Color.parseColor("#33b5e5"));
                menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu popupMenu) {
                        v.setColorFilter(oldFilter);
                    }
                });
            }

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
                case R.id.action_remove_from_queue: {
                        player.removeFromQueue(list.get(position));
                        Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return true;
                case R.id.action_add_to_queue: {
                        player.addToQueue(list.get(position));
                        Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return true;
                case R.id.set_as_ringtone:
                    Utils.setSongAsRingtone(context, list.get(position));
                    return true;
                default:
                    return false;
            }
        }
    }

    public SongsListAdapter(Context context, LocalPlayer player, List<Song> list) {
        super(context, R.layout.list_item_songs, list);
        this.context = context;
        this.list = list;
        this.player = player;
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

        return rowView;
    }



}
