package app.adapter.view_holders

import android.widget.Checkable
import android.widget.ImageView
import android.widget.TextView

import es.claucookie.miniequalizerlibrary.EqualizerView
import groovy.transform.CompileStatic

@CompileStatic
class SimpleSongViewHolder {
    public Checkable layout
    public TextView title
    public TextView duration
    public EqualizerView nowPlayingEqIcon
    public ImageView contextMenu
}