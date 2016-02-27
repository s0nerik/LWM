package app.adapter.albums

import android.view.LayoutInflater
import android.view.ViewGroup
import app.R
import app.adapter.base.MediaStoreItem
import app.model.Album
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic

@CompileStatic
class ArtistAlbumItem extends MediaStoreItem<ArtistAlbumViewHolder> {
    Album album

    ArtistAlbumItem(Album album) {
        super(album.id)
        this.album = album
    }

    @Override
    int getLayoutRes() {
        return R.layout.item_artists_album
    }

    @Override
    ArtistAlbumViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ArtistAlbumViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    @Override
    void bindViewHolder(FlexibleAdapter adapter, ArtistAlbumViewHolder holder, int position, List payloads) {
        holder.album = album
    }
}