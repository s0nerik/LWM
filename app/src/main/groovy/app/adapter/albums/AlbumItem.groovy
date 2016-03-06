package app.adapter.albums

import android.view.LayoutInflater
import android.view.ViewGroup
import app.R
import app.adapter.base.MediaStoreItem
import app.model.Album
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import groovy.transform.CompileStatic

@CompileStatic
class AlbumItem extends MediaStoreItem<AlbumViewHolder> implements IFilterable {
    Album album

    AlbumItem(Album album) {
        super(album.id)
        this.album = album
    }

    @Override
    int getLayoutRes() {
        return R.layout.item_albums
    }

    @Override
    AlbumViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new AlbumViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    @Override
    void bindViewHolder(FlexibleAdapter adapter, AlbumViewHolder holder, int position, List payloads) {
        holder.album = album
    }

    @Override
    boolean filter(String constraint) {
        return album.title?.toLowerCase()?.contains(constraint?.toLowerCase()) ||
                album.artistName?.toLowerCase()?.contains(constraint?.toLowerCase()) ||
                constraint?.isInteger() && album.year == (constraint as int)
    }
}