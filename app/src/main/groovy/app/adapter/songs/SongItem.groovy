package app.adapter.songs

import android.view.LayoutInflater
import android.view.ViewGroup
import app.Injector
import app.R
import app.Utils
import app.adapter.base.MediaStoreItem
import app.model.Song
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.inject.Inject

@CompileStatic
class SongItem extends MediaStoreItem<SongViewHolder> {

    @PackageScope
    @Inject
    Utils utils

    Song song

    SongItem(Song song) {
        super(song.id)
        Injector.inject this
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

        holder.title.text = song.title
        holder.artist.text = utils.getArtistName(song.artistName)
        holder.duration.text = song.durationString
    }
}