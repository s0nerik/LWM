package com.lwm.app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lwm.app.ui.fragment.FindStationsFragment;

public class PlayersAroundPagerAdapter extends FragmentPagerAdapter {

    String[] TITLES = {"Find station", "Favorite stations"};

    public PlayersAroundPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Fragment f = new FindStationsFragment();
                return f;
            case 1:
                Fragment f2 = new FindStationsFragment();
                return f2;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

}
