package app.ui.activity
import android.annotation.TargetApi
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import app.R
import app.Utils
import app.adapter.SimpleSongsListAdapter
import app.commands.EnqueueCommand
import app.commands.SetQueueAndPlayCommand
import app.data_managers.SongsManager
import app.events.player.playback.PlaybackStartedEvent
import app.events.player.service.CurrentSongAvailableEvent
import app.model.Album
import app.model.Song
import com.bumptech.glide.Glide
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.annotations.Extra
import com.github.s0nerik.betterknife.annotations.InjectView
import com.github.s0nerik.betterknife.annotations.OnClick
import com.melnykov.fab.FloatingActionButton
import com.nirhart.parallaxscroll.views.ParallaxScrollView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
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
    Utils utils;

    @Inject
    SongsManager songsManager

    @Extra
    Album album;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);
        BetterKnife.loadExtras(this);

        playlist = songsManager.loadAllSongs(album).toList().toBlocking().first();

        adapter = new SimpleSongsListAdapter(this, playlist);

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

        Glide.with(this)
                .load("file://" + album.getAlbumArtPath())
                .centerCrop()
                .error(R.drawable.no_cover)
                .placeholder(R.color.grid_item_default_bg)
                .into(mCover)
//                .withBitmapInfo()
//                .setCallback(SingleBitmapPaletteInfoCallback.builder()
//                                    .fab(mFab)
//                                    .build()
//                );

        String artistName = utils.getArtistName album.artistName
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
        bus.post new SetQueueAndPlayCommand(playlist, 0)
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
                bus.post new EnqueueCommand(playlist)
                Toast toast = Toast.makeText(this, R.string.album_added_to_queue, Toast.LENGTH_SHORT)
                toast.show()
                return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Debug.d("onItemClick: " + position);
        setSelection(position);

        bus.post new SetQueueAndPlayCommand(playlist, position)
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    public void highlightCurrentSong(Song song) {
        int pos = playlist.indexOf song
        selection = pos
    }

    private void setSelection(int position) {
        mListView.setItemChecked position, true
        adapter.setChecked position
    }

    @Subscribe
    void playbackStarted(PlaybackStartedEvent event) {
        highlightCurrentSong event.song
    }

    @Subscribe
    void currentSongAvailable(CurrentSongAvailableEvent event) {
        highlightCurrentSong event.song
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

                float scrolledPercent = scroll / parallaxArea as float;
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