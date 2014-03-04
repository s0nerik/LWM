package com.lwm.app.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.lwm.app.R;

import java.io.IOException;

public class AlbumArtAsyncGetter extends AsyncTask<Uri, Void, Void> {
    private Context context;
    private ImageView albumArt;
    private Bitmap cover;
    boolean found = true;

    public AlbumArtAsyncGetter(Context context, ImageView albumArt){
        this.context = context;
        this.albumArt = albumArt;
    }

    @Override
    protected Void doInBackground(Uri... uri) {
        try {
            cover = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri[0]);
        } catch (IOException e) {
//            e.printStackTrace();
            found = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(found)   albumArt.setImageBitmap(cover);
        else        albumArt.setImageResource(R.drawable.no_cover);
    }

}
