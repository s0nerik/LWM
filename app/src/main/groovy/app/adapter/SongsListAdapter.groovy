package app.adapter
import android.content.Context
import android.support.v7.widget.RecyclerView.Adapter
import android.view.View
import android.view.ViewGroup
import app.*
import app.R.layout
import app.adapter.view_holders.SongViewHolder
import app.commands.PlaySongAtPositionCommand
import app.commands.SetQueueAndPlayCommand
import app.model.Song
import com.squareup.otto.Bus
import groovy.transform.*

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class SongsListAdapter extends Adapter<SongViewHolder> {

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
        def holder = new SongViewHolder(View.inflate(context, layout.item_songs, null), songs)
        if (!newQueueOnClick) {
            holder.mContainer.onClickListener = { View v ->
                def pos = holder.getAdapterPosition()
                bus.post new PlaySongAtPositionCommand(pos)
            }
        } else {
            holder.mContainer.onClickListener = { View v ->
                def pos = holder.getAdapterPosition()
                bus.post new SetQueueAndPlayCommand(songs, pos)
            }
        }
        return holder
    }

    @Override
    void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = songs[position]
        holder.mTitle.text = song.title
        holder.mArtist.text = utils.getArtistName(song.artistName)
        holder.mDuration.text = song.durationString

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
