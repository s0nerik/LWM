package app.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import app.Injector;
import com.lwm.app.R;
import com.lwm.app.ui.fragment.AlbumsListFragmentBuilder;
import app.ui.fragment.ArtistsListFragment;
import app.ui.fragment.QueueFragment;
import app.ui.fragment.SongsListFragment;

import javax.inject.Inject;

public class LocalMusicFragmentsAdapter extends FragmentPagerAdapter {

    @Inject
    Resources resources;

    private String[] names;

    public LocalMusicFragmentsAdapter(FragmentManager fm) {
        super(fm);
        Injector.inject(this);
        names = resources.getStringArray(R.array.local_music_tabs);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return names[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SongsListFragment();
            case 1:
                return new ArtistsListFragment();
            case 2:
                return new AlbumsListFragmentBuilder().build();
            case 3:
                return new QueueFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return names.length;
    }
}
