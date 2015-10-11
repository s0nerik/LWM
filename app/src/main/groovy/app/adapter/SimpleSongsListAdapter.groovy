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
import app.Injector
import app.R
import app.Utils
import app.model.Song
import app.player.LocalPlayer
import es.claucookie.miniequalizerlibrary.EqualizerView
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class SimpleSongsListAdapter extends ArrayAdapter<Song> {

    private List<Song> songsList

    private int checked = -1

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    LayoutInflater inflater

    public SimpleSongsListAdapter(Context context, List<Song> playlist) {
        super(context, R.layout.list_item_songs_simple, playlist)
        songsList = playlist
        Injector.inject this
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
