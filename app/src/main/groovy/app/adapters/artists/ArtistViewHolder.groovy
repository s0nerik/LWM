package app.adapters.artists

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.App
import app.R
import app.Utils
import app.models.Artist
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.ExpandableViewHolder
import groovy.transform.CompileStatic
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
    protected Utils utils

    Artist artist

    ArtistViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
        App.get().inject this
        BetterKnife.inject this, view
    }

    void setArtist(Artist artist) {
        this.artist = artist

        def artistName = utils.getArtistName artist.name
        def shortName = artistName.replaceAll("\\p{P}|\\p{S}", "")
                                  .replaceAll("  ", " ")
                                  .split(" ")
                                  .collect { String s -> s[0] }
                                  .join("")

        int fontSize = 24 + Math.round(56f / (shortName.size()**1.25f)) as int

        def drawable = TextDrawable.builder()
                                   .beginConfig()
                                   .toUpperCase()
                                   .fontSize(fontSize)
                                   .endConfig()
                                   .buildRound(WordUtils.capitalize(shortName),
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