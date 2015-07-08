package app.adapter.view_holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.Injector
import app.R
import app.events.ui.ShouldStartArtistInfoActivity
import app.model.ArtistWrapper
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class ArtistViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.subtitle)
    TextView mSubtitle;
    @InjectView(R.id.imageView)
    ImageView mImageView;

    @Inject
    @PackageScope
    Bus bus

    List<ArtistWrapper> artistWrapperList

    ArtistViewHolder(View view, List<ArtistWrapper> artistWrapperList) {
        super(view);
        Injector.inject(this)
        BetterKnife.inject(this, view)

        this.artistWrapperList = artistWrapperList
    }

    @OnClick(R.id.itemLayout)
    public void onClick() {
        bus.post(new ShouldStartArtistInfoActivity(artist: artistWrapperList.get(getAdapterPosition()).getArtist()))
    }

}