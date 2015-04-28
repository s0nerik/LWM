package app.ui.activity;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arasthel.swissknife.SwissKnife;
import com.arasthel.swissknife.annotations.Extra;
import com.arasthel.swissknife.annotations.InjectView;
import com.arasthel.swissknife.annotations.OnClick;
import com.koushikdutta.ion.Ion;
import com.lwm.app.R;
import com.melnykov.fab.FloatingActionButton;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import app.Utils;
import app.adapter.SimpleSongsListAdapter;
import app.events.player.playback.PlaybackStartedEvent;
import app.events.player.service.CurrentSongAvailableEvent;
import app.helper.db.Order;
import app.helper.db.SongsCursorGetter;
import app.model.Album;
import app.model.Playlist;
import app.model.Song;
import app.player.LocalPlayer;
import ru.noties.debug.Debug;

public class AlbumInfoActivity extends BaseLocalActivity implements AdapterView.OnItemClickListener {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.cover)
    ImageView mCover;
    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    @InjectView(R.id.twoWayView)
    ListView mListView;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.subtitle)
    TextView mSubtitle;
    @InjectView(R.id.year)
    TextView mYear;
    @InjectView(R.id.scrollView)
    ParallaxScrollView mScrollView;
    @InjectView(R.id.overlay)
    LinearLayout mOverlay;

    private List<Song> playlist;

    private SimpleSongsListAdapter adapter;

    @Inject
    Bus bus;

    @Inject
    Resources resources;

    @Inject
    LayoutInflater inflater;

    @Inject
    LocalPlayer player;

    @Inject
    Utils utils;

    @Extra
    Album album;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);
        SwissKnife.inject(this);

        playlist = Playlist.fromCursor(new SongsCursorGetter().getSongsCursor(Order.ASCENDING, album));

        adapter = new SimpleSongsListAdapter(this, player, playlist);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        initHeader(album);

        setSupportActionBar(mToolbar);

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ActionBarColorOnScrollChangedListener());

        bus.register(this);
    }

    private void initHeader(Album album) {
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        Ion.with(mCover)
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .smartSize(true)
                .load("file://" + album.getAlbumArtPath());
//                .withBitmapInfo()
//                .setCallback(SingleBitmapPaletteInfoCallback.builder()
//                                    .floatingActionButton(mFab)
//                                    .build()
//                );

        String artistName = utils.getArtistName(album.getArtist());
        String title = String.valueOf(album.getTitle());

        int year = album.getYear();
        if (year != 0) {
            mYear.setText(String.valueOf(year));
        } else {
            mYear.setVisibility(View.GONE);
        }

        mTitle.setText(title);
        mSubtitle.setText(artistName);

        mToolbar.setTitle(title);
        mToolbar.setSubtitle(artistName);
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        if (player.isPlaying()) {
            player.stop();
        }
        player.setQueue(playlist);
        player.play(0);
    }

    @OnClick(R.id.overflowMenu)
    public void onOverflowMenuClicked(View view) {

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
        switch (id) {
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
        Debug.d("onItemClick: " + position);
        setSelection(position);

        player.setQueue(playlist);
        player.play(position);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    public void highlightCurrentSong() {
        int pos = Utils.getCurrentSongPosition(player, playlist);
        setSelection(pos);
    }

    private void setSelection(int position) {
        mListView.setItemChecked(position, true);
        adapter.setChecked(position);
    }

    @Subscribe
    public void playbackStarted(PlaybackStartedEvent event) {
        highlightCurrentSong();
    }

    @Subscribe
    public void currentSongAvailable(CurrentSongAvailableEvent event) {
        highlightCurrentSong();
    }

    @TargetApi(21)
    private class ActionBarColorOnScrollChangedListener implements ViewTreeObserver.OnScrollChangedListener {

        int lastScroll = 0;
        int abHeight = Utils.dpToPx(56);
        int primaryColor = resources.getColor(R.color.primary);
        int primaryDarkColor = resources.getColor(R.color.primaryDark);

        @Override
        public void onScrollChanged() {
            int scroll = mScrollView.getScrollY();
            int parallaxArea = mCover.getHeight() - abHeight;

            if (scroll >= parallaxArea && scroll > lastScroll) {

                mToolbar.setBackgroundColor(primaryColor);
                mToolbar.setTitleTextColor(Color.WHITE);
                mToolbar.setSubtitleTextColor(Color.WHITE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(primaryDarkColor);
                }

            } else if (scroll <= 0) { // Overscrolled

                mToolbar.setBackgroundColor(Color.TRANSPARENT);
                mToolbar.setTitleTextColor(Color.TRANSPARENT);
                mToolbar.setSubtitleTextColor(Color.TRANSPARENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.BLACK);
                }

            } else if (scroll <= parallaxArea) {

                Debug.d("Scroll: " + scroll);

                float scrolledPercent = scroll / (float) parallaxArea;
                int transparency = Math.round(scrolledPercent * 255f);

                mToolbar.setBackgroundColor(Color.argb(transparency,
                        Color.red(primaryColor),
                        Color.green(primaryColor),
                        Color.blue(primaryColor)
                ));
                mToolbar.setTitleTextColor(Color.argb(transparency, 255, 255, 255));
                mToolbar.setSubtitleTextColor(Color.argb(transparency, 255, 255, 255));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(
                            Utils.darkerColor(Color.rgb(
                                    Color.red(primaryDarkColor),
                                    Color.green(primaryDarkColor),
                                    Color.blue(primaryDarkColor)
                            ), scrolledPercent)
                    );
                }

            }

            lastScroll = scroll;
        }
    }

}