package app.adapter.view_holders
import android.content.Context
import android.graphics.ColorFilter
import android.os.Build
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import app.Injector
import app.R
import app.model.Song
import app.player.LocalPlayer
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import es.claucookie.miniequalizerlibrary.EqualizerView
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class SongViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.artist)
    TextView mArtist;
    @InjectView(R.id.duration)
    TextView mDuration;
    @InjectView(R.id.contextMenu)
    ImageView mContextMenu;
    @InjectView(R.id.playIcon)
    EqualizerView mPlayIcon;
    @InjectView(R.id.container)
    RelativeLayout mContainer;

    @PackageScope
    @Inject
    LocalPlayer player

    @PackageScope
    @Inject
    Context context

    List<Song> songs

    SongViewHolder(View view, List<Song> songs) {
        super(view)
        this.songs = songs
        Injector.inject(this)
        BetterKnife.inject(this, view)
    }

    @OnClick(R.id.contextMenu)
    public void onContextMenuClicked(View v) {
        def wrapper = new ContextThemeWrapper(context, R.style.AppTheme)
        def menu = new PopupMenu(wrapper, v);

        if (player.isSongInQueue(songs.get(getAdapterPosition()))) {
            menu.inflate(R.menu.songs_popup_in_queue);
        } else {
            menu.inflate(R.menu.songs_popup);
        }

        menu.setOnMenuItemClickListener(new OnContextMenuItemClickListener(getAdapterPosition(), songs));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final ImageView imageView = (ImageView) v;
            final ColorFilter oldFilter = imageView.getColorFilter();
            imageView.setColorFilter(context.getResources().getColor(R.color.accent));
            menu.onDismissListener = {PopupMenu m -> imageView.setColorFilter(oldFilter)}
        }

        menu.show();
    }

}