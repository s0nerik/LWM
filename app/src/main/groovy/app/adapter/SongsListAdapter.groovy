package app.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import app.Injector
import app.R
import app.Utils
import app.adapter.view_holders.SongViewHolder
import app.commands.PlaySongAtPositionCommand
import app.commands.StartPlaylistCommand
import app.model.Song
import com.squareup.otto.Bus
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class SongsListAdapter extends RecyclerView.Adapter<SongViewHolder> {

    private final Context context
    private List<Song> songs

    public int selection = -1

    private boolean newQueueOnClick = true;

    private boolean playing

    @Inject
    Utils utils

    @Inject
    Bus bus

    SongsListAdapter(Context context, List<Song> songs) {
        Injector.inject(this);
        this.context = context;
        this.songs = songs;
    }

    @Override
    SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        def holder = new SongViewHolder(View.inflate(context, R.layout.item_songs, null), songs)
        if (!newQueueOnClick) {
            holder.mContainer.onClickListener = { View v ->
                def pos = holder.getAdapterPosition()
                bus.post new PlaySongAtPositionCommand(pos)
            }
        } else {
            holder.mContainer.onClickListener = { View v ->
                def pos = holder.getAdapterPosition()
                bus.post new StartPlaylistCommand(songs, pos)
            }
        }
        return holder
    }

    @Override
    void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = songs[position]
        holder.mTitle.setText song.title
        holder.mArtist.setText utils.getArtistName(song.artist)
        holder.mDuration.setText song.durationString

        if (selection == position) {
            holder.mPlayIcon.setVisibility(View.VISIBLE);
            if (playing) {
                holder.mPlayIcon.animateBars();
            } else {
                holder.mPlayIcon.stopBars();
            }
        } else {
            holder.mPlayIcon.setVisibility(View.GONE);
        }

    }

    @Override
    int getItemCount() {
        return songs.size()
    }

    void setSelection(int position) {
        int oldSelection = selection
        selection = position

        notifyItemChanged oldSelection
        notifyItemChanged position
    }

    void updateEqualizerState(boolean playing) {
        this.playing = playing
        notifyItemChanged selection
    }

}
