package com.lwm.app.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lwm.app.R;
import com.lwm.app.helper.SongsCursorGetter;
import com.lwm.app.lib.QuickAdapter;

import java.io.FileDescriptor;

public class SongsCursorAdapter extends QuickAdapter {

    Context context;

    public SongsCursorAdapter(Context context, SongsCursorGetter cursorGetter) {
        super(context, cursorGetter);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);

        return inflater.inflate(R.layout.list_item_songs, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView title = (TextView) view.findViewById(R.id.songs_list_item_title);
        TextView artist = (TextView) view.findViewById(R.id.songs_list_item_artist);
        TextView duration = (TextView) view.findViewById(R.id.songs_list_item_duration);
//        ImageView coverAlbum = (ImageView) view.findViewById(R.id.songs_list_item_album_cover);

        title.setText(cursor.getString(SongsCursorGetter.TITLE));
        artist.setText(cursor.getString(SongsCursorGetter.ARTIST));
        int seconds = cursor.getInt(SongsCursorGetter.DURATION)/1000;
        int minutes = seconds/60;
        seconds -= minutes*60;
        duration.setText(minutes+":"+String.format("%02d", seconds));

//        /* Get art work from getAlbumart */
//        Bitmap art=getAlbumart(cursor.getLong(SongsCursorGetter.ALBUM_ID));
//        if(art!=null)
//            coverAlbum.setImageBitmap(art);
//        else
//            coverAlbum.setImageResource(R.drawable.ic_no_cover);

    }

    /**
     * Gets the albumart. fetches the album art and set's the image view to the reteived file
     *
     * @param album_id the album_id
     * @return the albumart
     */
    protected Bitmap getAlbumart(Long album_id)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {

        }
        return bm;
    }

}