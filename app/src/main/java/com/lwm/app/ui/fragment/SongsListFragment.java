package com.lwm.app.ui.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.Toast;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.events.player.binding.LocalPlayerServiceBoundEvent;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Song;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.async.SongsListLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class SongsListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<Song>> {

    private List<Song> songs;
    private LocalPlayerService player;
    private ListView listView;
    private int currentPosition = -1;

    private Loader<List<Song>> songsLoader;

    private boolean isFirstClick = true;

    private final static int SMOOTH_SCROLL_MAX = 50;

    public SongsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(App.TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);

        // KitKat fast-scroll workaround
        if (Build.VERSION.SDK_INT >= 19) {
            listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    listView.setFastScrollEnabled(true);
                    if (Build.VERSION.SDK_INT >= 16) {
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
            listView.setFastScrollEnabled(true);
        }

        if(App.isLocalPlayerServiceBound()) {
            player = App.getLocalPlayerService();
            initAdapter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getBus().register(this);
        if(App.localPlayerActive()) {
            player = App.getLocalPlayerService();
            highlightCurrentSong();
//            player.registerListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getBus().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.songs_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Subscribe
    public void onServiceBound(LocalPlayerServiceBoundEvent event){
        player = event.getLocalPlayerService();
        initAdapter();
    }

    public void highlightCurrentSong(){
        if(App.localPlayerActive()) {
            player = App.getLocalPlayerService();
            if (player.hasCurrentSong()) {
                Song song = player.getCurrentSong();
                int pos = songs.indexOf(song);
                if (pos != -1) {
                    setSelection(pos);

                    // TODO: replace this workaround
                    listView.setSelection(pos - 1);
                }
            }
        }
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        listView.setItemChecked(position, true);

        Log.d(App.TAG, "setSelection: "+currentPosition);

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

        if (App.isLocalPlayerServiceBound()) {
            player.setQueue(songs);
            player.play(position);
        }
    }

    private void initAdapter(){
        Log.d(App.TAG, "initAdapter()");
        songs = new ArrayList<>();
        SongsListAdapter adapter = new SongsListAdapter(getActivity(), songs);
        setListAdapter(adapter);
        songsLoader = new SongsListLoader(getActivity(), new SongsCursorGetter(getActivity()).getSongsCursor());
        getLoaderManager().initLoader(0, null, this);
        songsLoader.forceLoad();
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        Log.d(App.TAG, "onCreateLoader");
        getActivity().findViewById(android.R.id.empty).setVisibility(View.GONE);
        getActivity().findViewById(android.R.id.progress).setVisibility(View.VISIBLE);
        return songsLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        Log.d(App.TAG, "onLoadFinished()");
        getActivity().findViewById(android.R.id.progress).setVisibility(View.GONE);
        if(!data.isEmpty()) {
            songs.addAll(data);
            ((SongsListAdapter) getListAdapter()).notifyDataSetChanged();
            highlightCurrentSong();
        }else{
            getActivity().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        ((SongsListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_shuffle){
            shuffleAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shuffleAll(){
        if (songs != null && !songs.isEmpty()) {
            List<Song> queue = new ArrayList<>(songs);
            player.setQueue(queue);
            player.shuffleQueue();
            player.play(0);
            highlightCurrentSong();
        } else {
            Toast.makeText(getActivity(), R.string.nothing_to_shuffle, Toast.LENGTH_LONG).show();
        }
    }

}