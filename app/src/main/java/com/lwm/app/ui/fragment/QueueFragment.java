package com.lwm.app.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.player.LocalPlayer;

public class QueueFragment extends ListFragment {

    private LocalPlayer player;

    OnSongSelectedListener mCallback;

    private ListView listView;
    private int currentPosition = -1;

    private final static int SMOOTH_SCROLL_MAX = 50;

    public QueueFragment() {
        player = App.getMusicService().getLocalPlayer();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSongSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_queue, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);
        setListAdapter(new SongsListAdapter(getActivity(), player.getQueue()));
    }

    @Override
    public void onResume() {
        super.onResume();

        if(App.isMusicServiceBound()){
            int pos = player.getCurrentQueuePosition();
            setSelection(pos);
        }
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        listView.setItemChecked(position, true);

        if(Math.abs(position - currentPosition) <= SMOOTH_SCROLL_MAX){
            listView.smoothScrollToPosition(position);
        }else{
            listView.setSelection(position);
        }
        currentPosition = position;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(App.TAG, "SongsListFragment: onListItemClick");
        if(App.isMusicServiceBound()){
            player.play(position);
        }
        mCallback.onSongSelected(position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.queue, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_shuffle:
                player.shuffleQueue();
                player.play(0);
                ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
                setSelection(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}