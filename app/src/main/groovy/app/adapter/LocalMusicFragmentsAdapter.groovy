package app.adapter

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

import app.Injector
import app.R
import app.ui.fragment.AlbumsListFragment
import app.ui.fragment.ArtistsListFragment
import app.ui.fragment.QueueFragment
import app.ui.fragment.SongsListFragment

@CompileStatic
public class LocalMusicFragmentsAdapter extends FragmentPagerAdapter {

    @Inject
    @PackageScope
    Resources resources

    private String[] names

    LocalMusicFragmentsAdapter(FragmentManager fm) {
        super(fm)
        Injector.inject(this)
        names = resources.getStringArray(R.array.local_music_tabs)
    }

    @Override
    CharSequence getPageTitle(int position) {
        names[position]
    }

    @Override
    Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SongsListFragment()
            case 1:
                return new ArtistsListFragment()
            case 2:
                return new AlbumsListFragment()
            case 3:
                return new QueueFragment()
        }
        return null
    }

    @Override
    int getCount() {
        names.length
    }
}
