package app.adapter;

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
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lwm.app.R;
import app.Utils;
import app.model.Song;
import app.player.LocalPlayer;

import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SimpleSongsListAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private List<Song> songsList;

    private int checked = -1;

    private LocalPlayer player;

    private class OnContextButtonClickListener implements View.OnClickListener {
        private int position;

        private OnContextButtonClickListener(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View view) {
            PopupMenu menu = new PopupMenu(context, view);

            if (player.isSongInQueue(songsList.get(position))) {
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
                    player.removeFromQueue(songsList.get(position));
                    Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
                case R.id.action_add_to_queue: {
                    player.addToQueue(songsList.get(position));
                    Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
                case R.id.set_as_ringtone:
                    Utils.setSongAsRingtone(context, songsList.get(position));
                    return true;
                default:
                    return false;
            }
        }
    }

    public SimpleSongsListAdapter(Context context, LocalPlayer player, List<Song> playlist) {
        super(context, R.layout.list_item_songs_simple, playlist);
        this.context = context;
        this.player = player;
        songsList = playlist;
    }

    static class ViewHolder {
        public Checkable layout;
        public TextView title;
        public TextView duration;
        public EqualizerView nowPlayingEqIcon;
        public ImageView contextMenu;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.list_item_songs_simple, parent, false);
            holder = new ViewHolder();

            holder.layout = (Checkable) rowView.findViewById(R.id.layout);
            holder.title = (TextView) rowView.findViewById(R.id.title);
            holder.duration = (TextView) rowView.findViewById(R.id.duration);
            holder.nowPlayingEqIcon = (EqualizerView) rowView.findViewById(R.id.now_playing_icon);
            holder.contextMenu = (ImageView) rowView.findViewById(R.id.contextMenu);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Song song = songsList.get(position);
        holder.title.setText(song.getTitle());
        holder.duration.setText(song.getDurationString());

        holder.contextMenu.setOnClickListener(new OnContextButtonClickListener(position));

        if (checked == position){
//            holder.title.setFont(R.string.FONT_ROBOTO_BOLD);
            holder.title.setTextColor(Color.WHITE);
            holder.duration.setTextColor(Color.WHITE);
            holder.contextMenu.setColorFilter(Color.WHITE);
            holder.layout.setChecked(true);
        } else {
//            holder.title.setFont(R.string.FONT_ROBOTO_REGULAR);
            holder.title.setTextColor(Color.BLACK);
            holder.duration.setTextColor(Color.BLACK);
            holder.contextMenu.setColorFilter(Color.GRAY);
            holder.layout.setChecked(false);
        }

        return rowView;
    }

    public void setChecked(int checked) {
        this.checked = checked;
        notifyDataSetChanged();
    }

}
