package app.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.Injector
import app.R
import app.Utils
import app.events.ui.ShouldStartArtistInfoActivity
import app.model.Artist
import app.model.ArtistWrapper
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import org.apache.commons.lang3.text.WordUtils

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class ArtistWrappersAdapter extends RecyclerView.Adapter<ArtistWrappersAdapter.ViewHolder> {

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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(context, R.layout.item_artists, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
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

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_artists.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.subtitle)
        TextView mSubtitle;
        @InjectView(R.id.imageView)
        ImageView mImageView;

        ViewHolder(View view) {
            super(view);
            BetterKnife.inject(this, view);
        }

        @OnClick(R.id.itemLayout)
        public void onClick() {
            bus.post(new ShouldStartArtistInfoActivity(artist: artistWrapperList.get(getAdapterPosition()).getArtist()));
        }

    }

}
