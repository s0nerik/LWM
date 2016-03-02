package app.ui.fragment

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.Nullable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import app.PrefManager
import app.R
import app.adapter.LocalMusicFragmentsAdapter
import app.events.chat.ChatMessageReceivedEvent
import app.events.player.playback.PlaybackPausedEvent
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.playback.SongPlayingEvent
import app.events.ui.ChangeFabActionCommand
import app.events.ui.ShouldStartArtistInfoActivity
import app.helper.MenuTint
import app.service.StreamPlayerService
import app.ui.Croutons
import app.ui.activity.ArtistInfoActivity
import app.ui.base.DaggerFragment
import com.astuetz.PagerSlidingTabStrip
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.OnClick
import com.jakewharton.rxbinding.widget.RxTextView
import com.mypopsy.drawable.SearchCrossDrawable
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.Subscription

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
    FloatingActionButton fab
    NowPlayingFragment nowPlayingFragment
    ViewGroup searchView
    EditText searchText
    ImageView searchIcon
    View btnClose
    protected CoordinatorLayout coordinator

    private Subscription radialEqualizerViewSubscription

    private Intent localPlayerServiceIntent
    private Intent streamPlayerServiceIntent

    protected Closure fabAction = {}

    private boolean canShowFab = true

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

        nowPlayingFragment = childFragmentManager.findFragmentById(R.id.nowPlayingFragment) as NowPlayingFragment
        childFragmentManager.beginTransaction().hide(nowPlayingFragment).commit()

        tabs.onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            void onPageScrollStateChanged(int state) {
                if (!canShowFab) return
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        fab.hide()
                        break
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView()
        bus.unregister this
    }

    protected void initToolbar() {
        toolbar.setTitle getString(R.string.local_music)

        toolbar.inflateMenu R.menu.search
        toolbar.onMenuItemClickListener = {
            switch (it.itemId) {
                case R.id.action_search:
                    searchView.show()
                    return true
            }
            return false
        }

        MenuTint.on(toolbar.menu)
                .setMenuItemIconColor(resources.getColor(R.color.md_white))
                .apply(activity)

        initSearchView()
    }

    private void initSearchView() {
        def searchToggle = new SearchCrossDrawable(context)
        searchIcon.imageDrawable = searchToggle

        def searchTextState = RxTextView.textChanges(searchText).map { it.length() as boolean }
        def searchTextStateChange = searchTextState.startWith(false).buffer(2, 1).filter { it[0] != it[1] }

        searchIcon.onClick {
            if (searchToggle.progress > 0)
                searchText.text = ""
        }

        btnClose.onClick { searchView.hide() }

        searchTextStateChange
                .filter { !it[0] && it[1] }
                .subscribe {
            def animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 500
            animator.addUpdateListener {
                searchToggle.progress = it.animatedValue as float
            }
            animator.start()
        }

        searchTextStateChange
                .filter { it[0] && !it[1] }
                .subscribe {
            def animator = ValueAnimator.ofFloat(1f, 0f)
            animator.duration = 500
            animator.addUpdateListener {
                searchToggle.progress = it.animatedValue as float
            }
            animator.start()
        }
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        fabAction()
    }

    @Subscribe
    void onSongPlaybackPaused(PlaybackPausedEvent e) {
        radialEqualizerViewSubscription?.unsubscribe()
    }

    @Subscribe
    void onSongPlaying(SongPlayingEvent e) {
//        radialEqualizerView.randomize()
//        radialEqualizerView.value = (e.progress / (float) e.duration) * 100 as float
    }

    @Subscribe
    void onChangeFabAction(ChangeFabActionCommand c) {
        if (!canShowFab) return

        fabAction = c.action
        fab.imageResource = c.iconId
        fab.show()
    }

    @Subscribe
    void onSongPlaybackStarted(PlaybackStartedEvent e) {
        if (canShowFab) {
            fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                void onHidden(FloatingActionButton fab) {
                    nowPlayingFragment.show(getChildFragmentManager()).subscribe { int height ->
                        def a = new Animation() {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t) {
                                RelativeLayout.LayoutParams params = coordinator.layoutParams as RelativeLayout.LayoutParams
                                params.bottomMargin = height * interpolatedTime as int
                                coordinator.layoutParams = params
                            }
                        }

                        a.duration = 150
                        coordinator.startAnimation a
                    }
                }
            })
            canShowFab = false
        }
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