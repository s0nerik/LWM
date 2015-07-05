package app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.R
import app.Utils
import app.events.player.playback.SongChangedEvent
import app.model.Song
import app.player.LocalPlayer
import app.ui.activity.LocalPlaybackActivity
import app.ui.base.DaggerFragment
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.nineoldandroids.view.ViewHelper
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class NowPlayingFragment extends DaggerFragment {

    @Inject
    Utils utils;
    @Inject
    Bus bus;
    @Inject
    LocalPlayer player;

    @InjectView(R.id.cover)
    ImageView mCover;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.artist)
    TextView mArtist;
    @InjectView(R.id.now_playing_layout)
    View mOverlay;
    @InjectView(R.id.shadow)
    View mShadow;

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        Song song = player.getCurrentSong();
        if (song != null) {
            setSongInfo(player.getCurrentSong());
        }
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);
        BetterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public void setSongInfo(Song song) {
        Glide.with(mCover.context)
                .load(song.getAlbumArtUri().toString())
                .centerCrop()
                .placeholder(R.drawable.no_cover)
                .error(R.drawable.no_cover)
//                .withBitmapInfo()
//                .setCallback(new SingleBitmapPaletteInfoCallback(mOverlay, mShadow, mTitle, mArtist));

        // TODO: generate palette

        mArtist.setText(utils.getArtistName(song.getArtist()));
        mTitle.setText(song.getTitle());

        ViewHelper.setAlpha(mShadow, 0.9f);
        ViewHelper.setAlpha(mOverlay, 0.9f);
    }

    @OnClick([R.id.layout, R.id.cover])
    public void onLayoutClicked() {
        Intent intent = new Intent(getActivity(), LocalPlaybackActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha);
    }

    @Subscribe
    public void onSongChanged(SongChangedEvent event) {
        setSongInfo(event.getSong());
    }

}
