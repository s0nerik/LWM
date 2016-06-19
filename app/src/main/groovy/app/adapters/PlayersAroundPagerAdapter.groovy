package app.adapters
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import app.ui.fragment.FindStationsFragment
import groovy.transform.CompileStatic

@CompileStatic
class PlayersAroundPagerAdapter extends FragmentPagerAdapter {

    String[] TITLES = [ "Find station", "Favorite stations" ]

    PlayersAroundPagerAdapter(FragmentManager fm) {
        super(fm)
    }

    @Override
    Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FindStationsFragment()
            case 1:
                return new FindStationsFragment()
        }
        return null
    }

    @Override
    CharSequence getPageTitle(int position) { TITLES[position] }

    @Override
    int getCount() { 2 }

}
