package app.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import app.Injector
import app.R
import app.Utils
import app.adapter.view_holders.SongViewHolder
import app.model.Song
import app.player.LocalPlayer
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class SongsListAdapter extends RecyclerView.Adapter<SongViewHolder> {

    private final Context context;
    private List<Song> songs;

    public int selection = -1;

    private boolean newQueueOnClick;

    @Inject
    Utils utils;

    @Inject
    LocalPlayer player;

    public SongsListAdapter(Context context, List<Song> songs) {
        Injector.inject(this);
        this.context = context;
        this.songs = songs;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        def holder = new SongViewHolder(View.inflate(context, R.layout.item_songs, null), songs)
        if (newQueueOnClick) {
            holder.mContainer.onClickListener = { View v ->
                def pos = holder.getAdapterPosition()
                player.play(pos)
                setSelection(pos)
            }
        } else {
            holder.mContainer.onClickListener = { View v ->
                def pos = holder.getAdapterPosition()
                player.setQueue(songs);
                player.play(pos);
                setSelection(pos);
            }
        }
        return holder
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(utils.getArtistName(song.getArtist()));
        holder.mDuration.setText(song.getDurationString());

        if (selection == position) {
            holder.mPlayIcon.setVisibility(View.VISIBLE);
            if (player.isPlaying()) {
                holder.mPlayIcon.animateBars();
            } else {
                holder.mPlayIcon.stopBars();
            }
        } else {
            holder.mPlayIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setSelection(int position) {
        int oldSelection = selection;
        selection = position;

        notifyItemChanged(oldSelection);
        notifyItemChanged(position);
    }

    public void updateEqualizerState() {
        notifyItemChanged(selection);
    }

}
