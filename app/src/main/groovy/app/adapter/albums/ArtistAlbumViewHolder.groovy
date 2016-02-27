package app.adapter.albums

import android.view.View
import app.R
import app.model.Album
import com.github.s0nerik.betterknife.annotations.InjectView
import eu.davidea.flexibleadapter.FlexibleAdapter
import groovy.transform.CompileStatic

@CompileStatic
class ArtistAlbumViewHolder extends AlbumViewHolder {

    @InjectView(R.id.shadow_top)
    View shadowTop
    @InjectView(R.id.shadow_bottom)
    View shadowBottom

    ArtistAlbumViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter)
    }

    void setAlbum(Album album) {
        super.setAlbum album

        subtitle.text = album.year ? "$album.year • ${album.songs.size()} songs" : "${album.songs.size()} songs"

        shadowTop.hide()
        shadowBottom.hide()

        def item = mAdapter.getItem adapterPosition
        def parent = mAdapter.getExpandableOf item

        if (parent?.expanded) {
            shadowBottom.show()

            def childPos = parent.subItems.indexOf item
            if (childPos == 0) {
                shadowTop.show()
            }
//            else if (childPos == parent.subItems.size() - 1) {
//                shadowBottom.show()
//            }
        }
    }
}