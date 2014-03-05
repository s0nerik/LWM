package com.lwm.app.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.lwm.app.App;
import com.lwm.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

public class RemoteAlbumArtAsyncGetter extends AsyncTask<Void, Void, Void> {
    private ImageView albumArt;
    private Bitmap cover;
    boolean found = true;

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetAlbumArt = new HttpGet(App.SERVER_ADDRESS+App.CURRENT_ALBUMART);

    public RemoteAlbumArtAsyncGetter(ImageView albumArt){
        this.albumArt = albumArt;
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        try {
            HttpResponse response = httpclient.execute(httpGetAlbumArt);
            InputStream is = response.getEntity().getContent();
            cover = BitmapFactory.decodeStream(is);
            if(cover == null){
                found = false;
            }
        } catch (IOException e) {
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