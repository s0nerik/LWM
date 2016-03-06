package app.adapter.artists

import android.view.LayoutInflater
import android.view.ViewGroup
import app.R
import app.adapter.albums.ArtistAlbumItem
import app.adapter.base.MediaStoreExpandableItem
import app.model.Artist
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFilterable
import groovy.transform.CompileStatic

@CompileStatic
class ArtistItem extends MediaStoreExpandableItem<ArtistViewHolder, ArtistAlbumItem> implements IFilterable {
    Artist artist

    ArtistItem(Artist artist) {
        super(artist.id)
        this.artist = artist
    }

    @Override
    int getLayoutRes() {
        return R.layout.item_artists
    }

    @Override
    ArtistViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ArtistViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
    }

    @Override
    void bindViewHolder(FlexibleAdapter adapter, ArtistViewHolder holder, int position, List payloads) {
        holder.artist = artist
    }

    @Override
    boolean filter(String constraint) {
        return artist.name?.toLowerCase()?.contains(constraint.toLowerCase())
    }
}