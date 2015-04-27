package app.ui.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import app.PrefManager;
import com.lwm.app.R;
import app.adapter.LocalMusicFragmentsAdapter;
import app.events.chat.ChatMessageReceivedEvent;
import app.events.ui.ShouldStartArtistInfoActivity;
import app.helper.wifi.WifiAP;
import app.service.StreamPlayerService;
import app.ui.Croutons;
import app.ui.base.DaggerFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import com.tale.prettybundle.Activities;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LocalMusicFragment extends DaggerFragment {

    @Inject
    PrefManager prefManager;

    @Inject
    Resources resources;

    @Inject
    WifiAP wifiAP;

    @Inject
    Bus bus;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.tabs)
    PagerSlidingTabStrip mTabs;
    @InjectView(R.id.pager)
    ViewPager mPager;

    private Intent localPlayerServiceIntent;
    private Intent streamPlayerServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streamPlayerServiceIntent = new Intent(getActivity(), StreamPlayerService.class);
        getActivity().stopService(streamPlayerServiceIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.inject(this, v);
        initToolbar();
        bus.register(this);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPager.setAdapter(new LocalMusicFragmentsAdapter(getChildFragmentManager()));
        mTabs.setViewPager(mPager);
    }

    protected void initToolbar() {
        mToolbar.setTitle(getString(R.string.local_music));
        mToolbar.inflateMenu(R.menu.local_broadcast);
    }

    @Produce
    public Toolbar produceToolbar() {
        return mToolbar;
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        Croutons.messageReceived(getActivity(), event.getMessage());
    }

    @Subscribe
    public void onStartArtistInfoActivity(ShouldStartArtistInfoActivity event) {
        Intent intent = Activities.createArtistInfoActivityIntent(getActivity(), event.getArtist());
        startActivity(intent);
    }

}