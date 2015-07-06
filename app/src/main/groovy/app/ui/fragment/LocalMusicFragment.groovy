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
import app.events.ui.ShouldStartArtistInfoActivity
import app.helper.wifi.WifiAP
import app.service.StreamPlayerService
import app.ui.Croutons
import app.ui.activity.ArtistInfoActivity
import app.ui.base.DaggerFragment
import com.astuetz.PagerSlidingTabStrip
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
@InjectLayout(value = R.layout.fragment_local_music, injectAllViews = true)
public class LocalMusicFragment extends DaggerFragment {

    @Inject
    PrefManager prefManager

    @Inject
    WifiAP wifiAP

    @Inject
    Bus bus

    Toolbar toolbar
    PagerSlidingTabStrip tabs
    ViewPager pager

    private Intent localPlayerServiceIntent
    private Intent streamPlayerServiceIntent

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