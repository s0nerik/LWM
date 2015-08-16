package app.adapter

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

import app.Injector
import app.R
import app.ui.fragment.AlbumsListFragment
import app.ui.fragment.ArtistsListFragment
import app.ui.fragment.QueueFragment
import app.ui.fragment.SongsListFragment

@CompileStatic
class LocalMusicFragmentsAdapter extends FragmentStatePagerAdapter {

    @Inject
    @PackageScope
    Resources resources

    private String[] names

    private Fragment[] fragments = [new SongsListFragment(),
                                    new ArtistsListFragment(),
                                    new AlbumsListFragment(),
                                    new QueueFragment()] as Fragment[]

    LocalMusicFragmentsAdapter(FragmentManager fm) {
        super(fm)
        Injector.inject(this)
        names = resources.getStringArray(R.array.local_music_tabs)
    }

    @Override
    CharSequence getPageTitle(int position) { names[position] }

    @Override
    Fragment getItem(int position) { fragments[position] }

    @Override
    int getCount() { names.length }
}
