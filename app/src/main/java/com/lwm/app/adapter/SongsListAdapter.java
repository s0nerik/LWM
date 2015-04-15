package com.lwm.app.adapter;

import android.content.Context;
import android.graphics.ColorFilter;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SingleSelector;
import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import lombok.Getter;
import lombok.Setter;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.BaseViewHolder> {

    private final Context context;
    private List<Song> songs;

    private MultiSelector selector = new SingleSelector();

    @Getter
    private int selection = -1;

    @Setter
    private boolean newQueueOnClick;

    @Inject
    Utils utils;

    @Inject
    LocalPlayer player;

    public SongsListAdapter(Context context, List<Song> songs) {
        Injector.inject(this);
        this.context = context;
        this.songs = songs;
        selector.setSelectable(true);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (newQueueOnClick) {
            return new QueueViewHolder(View.inflate(context, R.layout.item_songs, null));
        } else {
            return new SongsViewHolder(View.inflate(context, R.layout.item_songs, null));
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(utils.getArtistName(song.getArtist()));
        holder.mDuration.setText(song.getDurationString());

//        if (selector.isSelected(position, 0)) {
        if (selection == position) {
            holder.mPlayIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mPlayIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setSelection(int position) {
//        int oldSelection;
//        if (selector.getSelectedPositions().size() > 0) {
//            oldSelection = selector.getSelectedPositions().get(0);
//        } else {
//            oldSelection = -1;
//        }
//
//        selector.setSelected(position, 0, true);
//

        int oldSelection = selection;
        selection = position;

        notifyItemChanged(oldSelection);
        notifyItemChanged(position);
    }

    private class OnContextMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private int position;

        private OnContextMenuItemClickListener(int pos) {
            position = pos;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_remove_from_queue: {
                    player.removeFromQueue(songs.get(position));
                    Toast toast = Toast.makeText(context, R.string.song_removed_from_queue, Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
                case R.id.action_add_to_queue: {
                    player.addToQueue(songs.get(position));
                    Toast toast = Toast.makeText(context, R.string.song_added_to_queue, Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
                case R.id.set_as_ringtone:
                    Utils.setSongAsRingtone(context, songs.get(position));
                    return true;
                default:
                    return false;
            }
        }
    }

    abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.artist)
        TextView mArtist;
        @InjectView(R.id.duration)
        TextView mDuration;
        @InjectView(R.id.contextMenu)
        ImageView mContextMenu;
        @InjectView(R.id.playIcon)
        ImageView mPlayIcon;
        @InjectView(R.id.container)
        RelativeLayout mContainer;

        BaseViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        @OnClick(R.id.contextMenu)
        public void onContextMenuClicked(View view) {
            PopupMenu menu = new PopupMenu(context, view);

            if (player.isSongInQueue(songs.get(getPosition()))) {
                menu.inflate(R.menu.songs_popup_in_queue);
            } else {
                menu.inflate(R.menu.songs_popup);
            }

            menu.setOnMenuItemClickListener(new OnContextMenuItemClickListener(getPosition()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                final ImageView v = (ImageView) view;
                final ColorFilter oldFilter = v.getColorFilter();
                v.setColorFilter(context.getResources().getColor(R.color.accent));
                menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu popupMenu) {
                        v.setColorFilter(oldFilter);
                    }
                });
            }

            menu.show();
        }

    }

    class SongsViewHolder extends BaseViewHolder {

        SongsViewHolder(View view) {
            super(view);
        }

        @OnClick(R.id.container)
        public void onItemClicked() {
            player.setQueue(songs);
            player.play(getPosition());

            setSelection(getPosition());
        }

    }

    class QueueViewHolder extends BaseViewHolder {

        QueueViewHolder(View view) {
            super(view);
        }

        @OnClick(R.id.container)
        public void onItemClicked() {
            player.play(getPosition());

            setSelection(getPosition());
        }

    }

}
