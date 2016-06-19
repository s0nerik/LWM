package app.adapters.songs

import android.view.LayoutInflater
import android.view.ViewGroup
import app.R
import app.adapters.base.MediaStoreItem
import app.models.Song
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import groovy.transform.CompileStatic

@CompileStatic
class SongItem extends MediaStoreItem<SongViewHolder> implements IFilterable {
    Song song

    SongItem(Song song) {
        super(song.id)
        this.song = song
    }

    @Override
    int getLayoutRes() {
        return R.layout.item_songs
    }

    @Override
    SongViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new SongViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    @Override
    void bindViewHolder(FlexibleAdapter adapter, SongViewHolder holder, int position, List payloads) {
        holder.song = song
    }

    @Override
    boolean filter(String constraint) {
        return song.title?.toLowerCase()?.contains(constraint.toLowerCase()) || song.artistName?.toLowerCase()?.contains(constraint.toLowerCase())
    }
}