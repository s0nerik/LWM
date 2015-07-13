package app.adapter
import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Typeface
import android.os.Build
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import app.R
import app.Utils
import app.model.Song
import app.player.LocalPlayer
import es.claucookie.miniequalizerlibrary.EqualizerView
import groovy.transform.CompileStatic

@CompileStatic
public class SimpleSongsListAdapter extends ArrayAdapter<Song> {

    private final Context context
    private List<Song> songsList

    private int checked = -1

    private LocalPlayer player

    private LayoutInflater inflater

//    private class OnContextButtonClickListener implements View.OnClickListener {
//        private int position
//
//        OnContextButtonClickListener(int pos) {
//            position = pos
//        }
//
//        @Override
//        public void onClick(View view) {
//            PopupMenu menu = new PopupMenu(context, view)
//
//            if (player.isSongInQueue(songsList.get(position))) {
//                menu.inflate(R.menu.songs_popup_in_queue)
//            } else {
//                menu.inflate(R.menu.songs_popup)
//            }
//
//            menu.onMenuItemClickListener = {
//                switch (it.getItemId()){
//                    case R.id.action_remove_from_queue:
//                        player.removeFromQueue(songsList.get(position));
//                        Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT);
//                        toast.show();
//                        return true;
//                    case R.id.action_add_to_queue:
//                        player.addToQueue(songsList.get(position));
//                        Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
//                        toast.show();
//                        return true;
//                    case R.id.set_as_ringtone:
//                        Utils.setSongAsRingtone(context, songsList.get(position));
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                final ImageView v = (ImageView) view;
//                final ColorFilter oldFilter = v.getColorFilter();
//                v.setColorFilter(Color.parseColor("#33b5e5"));
//                menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
//                    @Override
//                    public void onDismiss(PopupMenu popupMenu) {
//                        v.setColorFilter(oldFilter);
//                    }
//                });
//            }
//
//            menu.show();
//        }
//
//    }

    public SimpleSongsListAdapter(Context context, LocalPlayer player, List<Song> playlist) {
        super(context, R.layout.list_item_songs_simple, playlist)
        this.context = context
        this.player = player
        songsList = playlist
        inflater = LayoutInflater.from context
    }

    static class ViewHolder {
        public Checkable layout
        public TextView title
        public TextView duration
        public EqualizerView nowPlayingEqIcon
        public ImageView contextMenu
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View rowView = convertView;
        if (!rowView) {
            rowView = inflater.inflate R.layout.list_item_songs_simple, parent, false
            holder = new ViewHolder()

            holder.layout = (Checkable) rowView.findViewById(R.id.layout)
            holder.title = (TextView) rowView.findViewById(R.id.title)
            holder.duration = (TextView) rowView.findViewById(R.id.duration)
            holder.nowPlayingEqIcon = (EqualizerView) rowView.findViewById(R.id.now_playing_icon)
            holder.contextMenu = (ImageView) rowView.findViewById(R.id.contextMenu)

            rowView.setTag(holder)
        } else {
            holder = rowView.getTag() as ViewHolder
        }

        Song song = songsList[position]
        holder.title.text = song.title
        holder.duration.text = song.durationString

        holder.contextMenu.onClickListener = {
            PopupMenu menu = new PopupMenu(context, it)

            if (player.isSongInQueue(song)) {
                menu.inflate(R.menu.songs_popup_in_queue)
            } else {
                menu.inflate(R.menu.songs_popup)
            }

            menu.onMenuItemClickListener = {
                switch (it.getItemId()){
                    case R.id.action_remove_from_queue:
                        player.removeFromQueue(song)
                        Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT)
                        toast.show()
                        return true
                    case R.id.action_add_to_queue:
                        player.addToQueue(song)
                        Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT)
                        toast.show()
                        return true
                    case R.id.set_as_ringtone:
                        Utils.setSongAsRingtone(context, song)
                        return true
                    default:
                        return false
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                final ImageView v = it as ImageView
                final ColorFilter oldFilter = v.colorFilter

                v.colorFilter = Color.parseColor "#33b5e5"

                menu.onDismissListener = {
                    v.setColorFilter oldFilter
                }
            }

            menu.show()
        }

        if (checked == position) {
            holder.title.typeface = Typeface.DEFAULT_BOLD
            holder.duration.typeface = Typeface.DEFAULT_BOLD
            holder.title.textColor = Color.BLACK
            holder.duration.textColor = Color.BLACK
            holder.contextMenu.colorFilter = Color.BLACK
        } else {
            holder.title.typeface = Typeface.DEFAULT
            holder.duration.typeface = Typeface.DEFAULT
            holder.title.textColor = Color.DKGRAY
            holder.duration.textColor = Color.DKGRAY
            holder.contextMenu.colorFilter = Color.GRAY
        }

        return rowView
    }

    public void setChecked(int checked) {
        this.checked = checked
        notifyDataSetChanged()
    }

}
