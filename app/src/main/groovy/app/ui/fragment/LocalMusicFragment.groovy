package app.ui.fragment
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.Nullable
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import app.PrefManager
import app.R
import app.adapter.LocalMusicFragmentsAdapter
import app.events.chat.ChatMessageReceivedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.ui.ShouldStartArtistInfoActivity
import app.service.StreamPlayerService
import app.ui.Croutons
import app.ui.activity.ArtistInfoActivity
import app.ui.base.DaggerFragment
import at.grabner.circleprogress.CircleProgressView
import com.astuetz.PagerSlidingTabStrip
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.squareup.otto.*
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
@InjectLayout(value = R.layout.fragment_local_music, injectAllViews = true)
public class LocalMusicFragment extends DaggerFragment {

    @Inject
    @PackageScope
    PrefManager prefManager

    @Inject
    @PackageScope
    Bus bus

    Toolbar toolbar
    PagerSlidingTabStrip tabs
    ViewPager pager
    CircleProgressView circleProgress

    private Intent localPlayerServiceIntent
    private Intent streamPlayerServiceIntent

    private Closure fabAction = {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streamPlayerServiceIntent = new Intent(activity, StreamPlayerService)
        activity.stopService streamPlayerServiceIntent
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated view, savedInstanceState
        initToolbar()
        bus.register(this)
        pager.adapter = new LocalMusicFragmentsAdapter(childFragmentManager)
        tabs.viewPager = pager
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView()
        bus.unregister this
    }

    protected void initToolbar() {
        toolbar.setTitle getString(R.string.local_music)
        toolbar.inflateMenu R.menu.local_broadcast
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        fabAction()
    }

    @Subscribe
    void onSongPlaybackStarted(PlaybackStartedEvent e) {
        circleProgress.visibility = View.VISIBLE
    }

    @Subscribe
    void onSongPlaying(SongPlayingEvent e) {
        circleProgress.value = (e.progress / (float) e.duration) * 100 as float
    }

    @Produce
    public Toolbar produceToolbar() {
        return toolbar
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Croutons.messageReceived activity, event.message
    }

    @Subscribe
    public void onStartArtistInfoActivity(ShouldStartArtistInfoActivity event) {
        Intent intent = new Intent(activity, ArtistInfoActivity)
        intent.putExtra 'artist', event.artist as Parcelable
        startActivity intent
    }

}