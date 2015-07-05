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
import app.model.ArtistWrapper
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
public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistViewHolder> {

    private List<ArtistWrapper> artistWrapperList;

    private final Context context;

    @Inject
    Bus bus;

    @Inject
    Utils utils;

    public ArtistWrappersAdapter(Context context, List<ArtistWrapper> artists) {
        Injector.inject(this);
        artistWrapperList = artists;
        this.context = context;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ArtistViewHolder(View.inflate(context, R.layout.item_artists, null), artistWrapperList);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int i) {
        ArtistWrapper artistWrapper = artistWrapperList.get(i);
        Artist artist = artistWrapper.getArtist();
        String artistName = utils.getArtistName(artist.getName());
        holder.mTitle.setText(artistName);
        holder.mSubtitle.setText(artist.getNumberOfAlbums() + " albums, " + artist.getNumberOfSongs() + " songs");

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(WordUtils.capitalize(artistName.substring(0, 2)),
                        ColorGenerator.DEFAULT.getColor(artistName));

        holder.mImageView.setImageDrawable(drawable);

    }

    @Override
    public int getItemCount() {
        return artistWrapperList.size();
    }

}
