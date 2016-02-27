package app.adapter.artists

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.Injector
import app.R
import app.Utils
import app.model.Artist
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.squareup.otto.Bus
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.ExpandableViewHolder
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.lang3.text.WordUtils

import javax.inject.Inject

@CompileStatic
class ArtistViewHolder extends ExpandableViewHolder {
    @InjectView(R.id.title)
    TextView title
    @InjectView(R.id.subtitle)
    TextView subtitle
    @InjectView(R.id.imageView)
    ImageView imageView

    @Inject
    @PackageScope
    Bus bus

    @Inject
    @PackageScope
    Utils utils

    Artist artist

    ArtistViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
        Injector.inject this
        BetterKnife.inject this, view
    }

    void setArtist(Artist artist) {
        this.artist = artist

        def artistName = utils.getArtistName artist.name

        def drawable = TextDrawable.builder()
                                   .buildRound(WordUtils.capitalize(artistName[0..1]),
                                               ColorGenerator.DEFAULT.getColor(artistName))

        title.text = artistName
        subtitle.text = "$artist.numberOfAlbums albums, $artist.numberOfSongs songs"
        imageView.imageDrawable = drawable
    }

//    @OnClick(R.id.itemLayout)
//    void onClick() {
//        bus.post new ShouldStartArtistInfoActivity(artist: artist)
//    }

}