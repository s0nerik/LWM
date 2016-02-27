package app.adapter.albums

import app.R
import app.model.Album
import groovy.transform.CompileStatic

@CompileStatic
class ArtistAlbumItem extends AlbumItem {
    ArtistAlbumItem(Album album) {
        super(album)
    }

    @Override
    int getLayoutRes() {
        return R.layout.item_artists_album
    }
}