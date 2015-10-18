package app.adapter
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import app.Injector
import app.R
import app.Utils
import app.adapter.view_holders.ArtistViewHolder
import app.model.Artist
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.apache.commons.lang3.text.WordUtils

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class ArtistsAdapter extends RecyclerView.Adapter<ArtistViewHolder> {

    private final List<Artist> artists

    private final Context context

    @Inject
    Bus bus

    @Inject
    Utils utils

    ArtistsAdapter(Context context, List<Artist> artists) {
        Injector.inject(this)
        this.artists = artists
        this.context = context
    }

    @Override
    ArtistViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        new ArtistViewHolder(View.inflate(context, R.layout.item_artists, null), artists)
    }

    @Override
    void onBindViewHolder(ArtistViewHolder holder, int i) {
        Artist artist = artists[i]
        String artistName = utils.getArtistName artist.name

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(WordUtils.capitalize(artistName.substring(0, 2)),
                        ColorGenerator.DEFAULT.getColor(artistName))

        holder.mTitle.text = artistName
        holder.mSubtitle.text = "$artist.numberOfAlbums albums, $artist.numberOfSongs songs"
        holder.mImageView.imageDrawable = drawable
    }

    @Override
    int getItemCount() {
        return artists.size()
    }

}
