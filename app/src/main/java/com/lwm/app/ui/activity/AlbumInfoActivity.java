package com.lwm.app.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.adapter.SimplePlaylistAdapter;
import com.lwm.app.helper.AlbumsCursorGetter;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.model.Album;
import com.lwm.app.model.Playlist;
import com.lwm.app.ui.fragment.OnSongSelectedListener;
import com.manuelpeinado.fadingactionbar.extras.actionbarcompat.FadingActionBarHelper;

public class AlbumInfoActivity extends ActionBarActivity implements OnSongSelectedListener {

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

        ListView listView = (ListView) findViewById(android.R.id.list);

        long albumId = getIntent().getIntExtra("album_id", -1);
        assert albumId != -1 : "albumId == -1";
        listView.setAdapter(new SimplePlaylistAdapter(this, new Playlist(new SongsCursorGetter(this).getSongs(albumId))));

        ImageView header = (ImageView) findViewById(R.id.image_header);
        Album album = new AlbumsCursorGetter(this).getAlbumById(albumId);

        String uri = album.getAlbumArtUri();
        if(uri != null) {
            header.setImageURI(Uri.parse(uri));
        }else{
            header.setImageResource(R.drawable.no_cover);
        }

        String artistName = album.getArtist();
        String title = String.valueOf(album.getTitle());

        TextView year = (TextView) findViewById(R.id.year);
        TextView songs = (TextView) findViewById(R.id.songs_count);

        year.setText(String.valueOf(album.getYear()));
        songs.setText(String.valueOf(album.getSongsCount()));

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSongSelected(int position) {
        // TODO: this
    }
}
