package app.adapter.view_holders

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.view.MenuItem
import android.widget.Toast
import app.Injector
import app.R
import app.Utils
import app.model.Song
import app.player.LocalPlayer
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class OnContextMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
    private int position;
    List<Song> songs;

    @PackageScope
    @Inject
    LocalPlayer player;

    @PackageScope
    @Inject
    Context context

    OnContextMenuItemClickListener(int pos, List<Song> songs) {
        Injector.inject(this)
        position = pos
        this.songs = songs
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_remove_from_queue:
                player.removeFromQueue(songs.get(position));
                Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.action_add_to_queue:
                player.addToQueue(songs.get(position));
                Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.set_as_ringtone:
                Utils.setSongAsRingtone(context, songs.get(position));
                return true;
            default:
                return false;
        }
    }
}