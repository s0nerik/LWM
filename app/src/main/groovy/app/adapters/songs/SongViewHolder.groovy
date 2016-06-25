package app.adapters.songs

import android.content.Context
import android.graphics.ColorFilter
import android.os.Build
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.PopupMenu
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import app.App
import app.R
import app.Utils
import app.commands.RequestPlaySongCommand
import app.helpers.CollectionManager
import app.models.Song
import app.players.LocalPlayer
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
class SongViewHolder extends FlexibleViewHolder {
    @InjectView(R.id.title)
    TextView title
    @InjectView(R.id.artist)
    TextView artist
    @InjectView(R.id.duration)
    TextView duration
    @InjectView(R.id.contextMenu)
    ImageView contextMenu
//    @InjectView(R.id.playIcon)
//    EqualizerView playIcon
    @InjectView(R.id.container)
    RelativeLayout container

    @Inject
    protected LocalPlayer player

    @Inject
    protected CollectionManager collectionManager

    @Inject
    protected Bus bus

    @Inject
    protected Context context

    @Inject
    protected Utils utils

    Song song

    SongViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
        App.get().inject this
        BetterKnife.inject this, view
    }

    void setSong(Song song) {
        this.song = song

        title.text = song.title
        artist.text = utils.getArtistName(song.artistName)
        duration.text = song.durationString

        updateSelectedState()
    }

    void updateSelectedState() {
//        if (mAdapter.isSelected(adapterPosition)) {
//            playIcon.show()
//        } else {
//            playIcon.hide()
//        }
    }

    @OnClick(R.id.contextMenu)
    void onContextMenuClicked(View v) {
        def wrapper = new ContextThemeWrapper(context, R.style.AppTheme)
        def menu = new PopupMenu(wrapper, v)

        if (player.isSongInQueue(song)) {
            menu.inflate(R.menu.songs_popup_in_queue)
        } else {
            menu.inflate(R.menu.songs_popup)
        }

        menu.onMenuItemClickListener = {
            switch (it.itemId) {
                case R.id.action_remove_from_queue:
                    player.removeFromQueue song
                    Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT)
                    toast.show()
                    return true
                case R.id.action_add_to_queue:
                    player.addToQueue song
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
            final ImageView imageView = (ImageView) v
            final ColorFilter oldFilter = imageView.getColorFilter()
            imageView.setColorFilter(context.getResources().getColor(R.color.accent))
            menu.onDismissListener = {PopupMenu m -> imageView.setColorFilter(oldFilter)}
        }

        menu.show()
    }

    @OnClick(R.id.container)
    void onClicked() {
        bus.post new RequestPlaySongCommand(song)
        mAdapter.toggleSelection(adapterPosition)
        updateSelectedState()
    }
}