package app.adapters.albums

import android.content.Intent
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.App
import app.R
import app.Utils
import app.models.Album
import app.ui.activity.AlbumInfoActivity
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
class AlbumViewHolder extends FlexibleViewHolder {
    @InjectView(R.id.cover)
    ImageView cover
    @InjectView(R.id.title)
    TextView title
    @InjectView(R.id.subtitle)
    TextView subtitle
    @InjectView(R.id.bottom_bar)
    LinearLayout bottomBar
    @InjectView(R.id.shadow)
    View shadow
    @InjectView(R.id.layout)
    View layout

    @Inject
    protected Utils utils

    protected Album album

    AlbumViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
        App.get().inject this
        BetterKnife.inject this, view
    }

    void setAlbum(Album album) {
        this.album = album

        title.text = album.title
        subtitle.text = utils.getArtistName album.artistName
//        holder.mBottomBar.setBackgroundResource(R.color.grid_item_default_bg);

        String url = "file://${album.albumArtPath}"
        Glide.with(cover.context)
             .load(url)
             .centerCrop()
             .error(R.drawable.no_cover)
             .placeholder(R.color.grid_item_default_bg)
             .crossFade()
             .into(cover)
    }

    @OnClick(R.id.layout)
    void onClick() {
        def intent = new Intent(layout.context, AlbumInfoActivity)
        intent.putExtra "album", album as Parcelable
        layout.context.startActivity intent
//        activity.overridePendingTransition R.anim.slide_in_right, R.anim.slide_out_left_long_alpha
    }

}