package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.lwm.app.R;
import com.lwm.app.adapter.PlayersAroundPagerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PlayersAroundFragment extends Fragment {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.tabs)
    PagerSlidingTabStrip mTabs;
    @InjectView(R.id.pager)
    ViewPager mPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_players_around, container, false);
        ButterKnife.inject(this, v);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setTitle(getString(R.string.stations_around));

        mPager.setAdapter(new PlayersAroundPagerAdapter(getChildFragmentManager()));

        mTabs.setViewPager(mPager);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
