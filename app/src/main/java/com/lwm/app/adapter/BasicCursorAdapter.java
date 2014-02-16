package com.lwm.app.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.io.FileDescriptor;

public abstract class BasicCursorAdapter extends CursorAdapter {

    protected Context context;

    public BasicCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    protected static abstract class ViewHolder{}

    @Override
    public abstract View newView(Context context, Cursor cursor, ViewGroup viewGroup);

    @Override
    public abstract void bindView(View view, Context context, Cursor cursor);


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
