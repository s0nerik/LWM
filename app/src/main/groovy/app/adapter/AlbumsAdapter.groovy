package app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import app.Injector
import app.R
import app.Utils
import app.adapter.view_holders.AlbumViewHolder
import app.model.Album
import com.bumptech.glide.Glide
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget

import javax.inject.Inject

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
public class AlbumsAdapter extends ArrayAdapter<Album> {

    private final Context context;
    private List<Album> albumsList;

    @Inject
    Utils utils;

    @Inject
    LayoutInflater inflater;

    public AlbumsAdapter(final Context context, List<Album> albums) {
        super(context, R.layout.item_songs, albums);
        Injector.inject(this);
        this.context = context;
        albumsList = albums;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AlbumViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_albums, parent, false);
            holder = new AlbumViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (AlbumViewHolder) rowView.getTag();
        }

        Album album = albumsList.get(position);
        holder.mTitle.setText(album.getTitle());
        holder.mSubtitle.setText(utils.getArtistName(album.getArtist()));
        holder.mBottomBar.setBackgroundResource(R.color.grid_item_default_bg);

        Glide.with(holder.mCover.context)
                .load("file://" + album.getAlbumArtPath())
                .centerCrop()
                .placeholder(R.color.grid_item_default_bg)
                .error(R.drawable.no_cover)
                .into(holder.mCover)
//                .withBitmapInfo()
//                .setCallback(new SingleBitmapPaletteInfoCallback(holder.mBottomBar, holder.mShadow, holder.mTitle, holder.mSubtitle));

        // TODO: generate palette

        return rowView;
    }

}
