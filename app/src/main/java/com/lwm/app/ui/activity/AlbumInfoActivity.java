package com.lwm.app.ui.activity;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.adapter.SimpleSongsListAdapter;
import com.lwm.app.events.player.playback.PlaybackStartedEvent;
import com.lwm.app.helper.db.AlbumsCursorGetter;
import com.lwm.app.helper.db.SongsCursorGetter;
import com.lwm.app.model.Album;
import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

public class AlbumInfoActivity extends BaseLocalActivity implements AdapterView.OnItemClickListener {

    private List<Song> playlist;
    private ListView listView;

    private SimpleSongsListAdapter adapter;
    private int screenWidth;

    @Inject
    Bus bus;

    @Inject
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FadingActionBarHelper fadingActionBarHelper = new FadingActionBarHelper()
                .actionBarBackground(R.drawable.ab_solid)
                .headerLayout(R.layout.album_info_header)
                .headerOverlayLayout(R.layout.album_info_header_overlay)
                .contentLayout(R.layout.activity_album_info);

        setContentView(fadingActionBarHelper.createView(this));
        fadingActionBarHelper.initActionBar(this);

        listView = (ListView) findViewById(android.R.id.list);

        long albumId = getIntent().getIntExtra("album_id", -1);
        assert albumId != -1 : "albumId == -1";
        playlist = Playlist.fromCursor(new SongsCursorGetter().getSongsCursor(albumId));
        adapter = new SimpleSongsListAdapter(this, player, playlist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        Album album = new AlbumsCursorGetter().getAlbumById(albumId);

        screenWidth = getResources().getDisplayMetrics().widthPixels;

        initHeader(album);

        bus.register(this);
    }

    private void initHeader(Album album){
        ImageView header = (ImageView) findViewById(R.id.image_header);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.primary)));

        Ion.with(header)
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .smartSize(true)
                .load("file://"+album.getAlbumArtPath());

        String artistName = album.getArtist();
        String title = String.valueOf(album.getTitle());

        TextView yearTV = (TextView) findViewById(R.id.year);
        TextView songsTV = (TextView) findViewById(R.id.songs_count);

        int year = album.getYear();
        if(year != 0){
            yearTV.setText(String.valueOf(year));
        }else{
            yearTV.setText(getResources().getString(R.string.year_unknown));
        }

        songsTV.setText(String.valueOf(album.getSongsCount()));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(android.R.color.transparent);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        actionBar.setCustomView(R.layout.actionbar_album);

        View customView = actionBar.getCustomView();
        TextView customTitle = (TextView) customView.findViewById(R.id.title);
        TextView customSubtitle = (TextView) customView.findViewById(R.id.subtitle);

        customTitle.setText(title);
        customSubtitle.setText(artistName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_info, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left_33_alpha, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_add_to_queue:
                player.addToQueue(playlist);
                Toast toast = Toast.makeText(this, R.string.album_added_to_queue, Toast.LENGTH_SHORT);
                toast.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(App.TAG, "onItemClick: "+position);
        setSelection(position-1);

        player.setQueue(playlist);
        player.play(position-1);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    public void highlightCurrentSong(){
        int pos = Utils.getCurrentSongPosition(player, playlist);
        setSelection(pos);
    }

    private void setSelection(int position) {
        listView.setItemChecked(position+1, true);
        adapter.setChecked(position);
    }

    @Subscribe
    public void playbackStarted(PlaybackStartedEvent event) {
        showNowPlayingBar(true);
        highlightCurrentSong();
    }

}