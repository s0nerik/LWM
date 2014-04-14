package com.lwm.app.ui.fragment;

import android.app.Activity;
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
import android.widget.ListView;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.PlayerListener;
import com.lwm.app.ui.async.SongsListLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    OnSongSelectedListener mCallback;

    private List<Song> songs;
    private LocalPlayer player;
    private ListView listView;
    private int currentPosition = -1;

    private Loader<List<Song>> songsLoader;

    private boolean isFirstClick = true;

    private final static int SMOOTH_SCROLL_MAX = 50;

    public SongsListFragment() {}

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);
    }

    @Override
    public void onStart() {
        super.onStart();
        long start = System.currentTimeMillis();
        initAdapter();
        long res = System.currentTimeMillis()-start;
        Log.d(App.TAG, "onStart: "+res+" ms.");
    }

    @Override
    public void onResume() {
        super.onResume();
        highlightCurrentSong();

//        if(App.isMusicServiceBound()){
//            if(LocalPlayer.hasCurrentSong()) {
//                Song song = LocalPlayer.getCurrentSong();
//                int pos = songs.indexOf(song);
////                int pos = LocalPlayer.getCurrentQueuePosition();
//                if(pos != -1) {
//                    listView.setItemChecked(pos, true);
//                    listView.setSelection(pos);
//                    currentPosition = pos;
//                }
//            }
//        }
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

    public void highlightCurrentSong(){
        if(LocalPlayer.hasCurrentSong()) {
            Song song = LocalPlayer.getCurrentSong();
            int pos = songs.indexOf(song);
            if(pos != -1) {
                listView.setItemChecked(pos, true);
                listView.setSelection(pos);
                currentPosition = pos;
            }
        }
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        listView.setItemChecked(position, true);

        Log.d(App.TAG, "setSelection: "+Math.abs(position - currentPosition));

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

        if (App.isMusicServiceBound()) {

            LocalPlayer player;
            if (isFirstClick) {
                player = new LocalPlayer(getActivity(), songs);
                App.getMusicService().setLocalPlayer(player);
                isFirstClick = false;
            } else {
                player = App.getMusicService().getLocalPlayer();
            }

            player.play(position);
        }
        mCallback.onSongSelected(position);
    }

    private void initAdapter(){
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
        return songsLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        songs.addAll(data);
        ((SongsListAdapter) getListAdapter()).notifyDataSetChanged();
        highlightCurrentSong();
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        songs.clear();
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
        List<Song> queue = new ArrayList<>(songs);
        Collections.shuffle(queue);
        player = new LocalPlayer(getActivity(), queue);
        App.getMusicService().setLocalPlayer(player);
        player.registerListener((PlayerListener) getActivity());
        player.play(0);
        highlightCurrentSong();
    }

}