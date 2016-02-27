package app.adapter.view_holders

import android.content.Context
import android.graphics.ColorFilter
import android.os.Build
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.PopupMenu
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import app.Injector
import app.R
import app.commands.PlaySongAtPositionCommand
import app.model.Song
import app.player.LocalPlayer
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import es.claucookie.miniequalizerlibrary.EqualizerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class SongViewHolder extends FlexibleViewHolder {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.artist)
    TextView artist;
    @InjectView(R.id.duration)
    TextView duration;
    @InjectView(R.id.contextMenu)
    ImageView contextMenu;
    @InjectView(R.id.playIcon)
    EqualizerView playIcon;
    @InjectView(R.id.container)
    RelativeLayout container;

    @PackageScope
    @Inject
    LocalPlayer player

    @PackageScope
    @Inject
    Bus bus

    @PackageScope
    @Inject
    Context context

    Song song

    SongViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
        Injector.inject(this)
        BetterKnife.inject(this, view)
    }

    @OnClick(R.id.contextMenu)
    public void onContextMenuClicked(View v) {
        def wrapper = new ContextThemeWrapper(context, R.style.AppTheme)
        def menu = new PopupMenu(wrapper, v)

        if (player.isSongInQueue(song)) {
            menu.inflate(R.menu.songs_popup_in_queue);
        } else {
            menu.inflate(R.menu.songs_popup);
        }

        menu.setOnMenuItemClickListener(new OnContextMenuItemClickListener(song));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final ImageView imageView = (ImageView) v;
            final ColorFilter oldFilter = imageView.getColorFilter();
            imageView.setColorFilter(context.getResources().getColor(R.color.accent));
            menu.onDismissListener = {PopupMenu m -> imageView.setColorFilter(oldFilter)}
        }

        menu.show();
    }

    @OnClick(R.id.container)
    public void onClicked() {
//            player.prepare(song)
        bus.post new PlaySongAtPositionCommand(PlaySongAtPositionCommand.PositionType.EXACT, adapterPosition)
    }

}