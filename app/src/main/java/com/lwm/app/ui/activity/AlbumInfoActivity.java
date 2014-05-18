package com.lwm.app.ui.activity;

import android.net.Uri;
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

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.SimpleSongsListAdapter;
import com.lwm.app.helper.AlbumsCursorGetter;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Album;
import com.lwm.app.model.Playlist;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.fragment.OnSongSelectedListener;
import com.manuelpeinado.fadingactionbar.extras.actionbarcompat.FadingActionBarHelper;

import java.util.List;

public class AlbumInfoActivity extends BasicActivity implements AdapterView.OnItemClickListener {

    private List<Song> playlist;
    private ListView listView;

    private SimpleSongsListAdapter adapter;

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
        playlist = Playlist.fromCursor(new SongsCursorGetter(this).getSongsCursor(albumId));
        adapter = new SimpleSongsListAdapter(this, playlist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        Album album = new AlbumsCursorGetter(this).getAlbumById(albumId);

        initHeader(album);
    }

    private void initHeader(Album album){
        ImageView header = (ImageView) findViewById(R.id.image_header);

        String uri = album.getAlbumArtUri();
        if(uri != null) {
            header.setImageURI(Uri.parse(uri));
        }else{
            header.setImageResource(R.drawable.no_cover);
        }

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
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        actionBar.setCustomView(R.layout.actionbar_album);

        View customView = actionBar.getCustomView();
        TextView customTitle = (TextView) customView.findViewById(R.id.title);
        TextView customSubtitle = (TextView) customView.findViewById(R.id.subtitle);

        customTitle.setText(title);
        customSubtitle.setText(artistName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.album_info, menu);
        return true;
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
        listView.setItemChecked(position, true);
        adapter.setChecked(position-1);

        LocalPlayer player = new LocalPlayer(this, playlist);
        App.getMusicService().setLocalPlayer(player);
        player.registerListener(this);
        player.play(position-1);
    }

}