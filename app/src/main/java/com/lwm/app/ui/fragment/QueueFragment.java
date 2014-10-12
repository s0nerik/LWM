package com.lwm.app.ui.fragment;

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
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.events.player.SongAddedToQueueEvent;
import com.lwm.app.service.LocalPlayerService;
import com.squareup.otto.Subscribe;

public class QueueFragment extends ListFragment {

    private LocalPlayerService player;

    private ListView listView;
    private int currentPosition = -1;

    private final static int SMOOTH_SCROLL_MAX = 50;

    private ListAdapter adapter;

    public QueueFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        App.getBus().register(this);
    }

    @Override
    public void onDestroy() {
        App.getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_queue, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);
        if (App.localPlayerActive() && !App.getLocalPlayerService().getQueue().isEmpty()) {
            adapter = new SongsListAdapter(getActivity(), App.getLocalPlayerService().getQueue());
            setListAdapter(adapter);
        } else {
//            view.findViewById(R.id.empty_queue_layout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(App.localPlayerActive()){
            int pos = App.getLocalPlayerService().getCurrentQueuePosition();
            setSelection(pos);
        }
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        listView.setItemChecked(position, true);

        if(Math.abs(position - currentPosition) <= SMOOTH_SCROLL_MAX){
            listView.smoothScrollToPosition(position-1);
        }else{
            listView.setSelection(position-1);
        }
        currentPosition = position;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(App.TAG, "SongsListFragment: onListItemClick");
        if(App.localPlayerActive()){
            App.getLocalPlayerService().play(position);
        }
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
                if(App.localPlayerActive()) {
                    player = App.getLocalPlayerService();
                    player.shuffleQueue();
                    player.play(0);
                    ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
                    setSelection(0);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onSongAddedToQueueEvent(SongAddedToQueueEvent event) {
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }

}