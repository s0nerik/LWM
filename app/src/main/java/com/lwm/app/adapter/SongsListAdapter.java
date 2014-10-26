package com.lwm.app.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
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

import java.io.File;
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

            if (App.getLocalPlayerService().isSongInQueue(list.get(position))) {
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
                        LocalPlayerService player = App.getLocalPlayerService();
                        player.removeFromQueue(list.get(position));
                        Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return true;
                case R.id.action_add_to_queue: {
                        LocalPlayerService player = App.getLocalPlayerService();
                        player.addToQueue(list.get(position));
                        Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return true;
                case R.id.set_as_ringtone:
                    setSongAsRingtone(position);
                    return true;
                default:
                    return false;
            }
        }
    }

    private void setSongAsRingtone(int pos) {
        Song song = list.get(pos);

        File newRingtone = new File(song.getSource());

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, newRingtone.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, newRingtone.length());
        values.put(MediaStore.MediaColumns.TITLE, song.getTitle());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.DURATION, song.getDuration());
        values.put(MediaStore.Audio.Media.ARTIST, song.getArtist());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(newRingtone.getAbsolutePath());
        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + newRingtone.getAbsolutePath() + "\"", null);
        Uri newUri = context.getContentResolver().insert(uri, values);

        try {
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
            Toast.makeText(context, String.format(context.getString(R.string.format_ringtone), song.getTitle()), Toast.LENGTH_LONG).show();
        } catch (Throwable t) {}
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

        return rowView;
    }



}
