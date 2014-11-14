package com.lwm.app.ui.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.enrique.stackblur.StackBlurManager;
import com.lwm.app.R;
import com.lwm.app.SupportAsyncTask;
import com.lwm.app.server.StreamServer;
import com.lwm.app.ui.fragment.playback.PlaybackFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

public class RemoteAlbumArtAsyncGetter extends SupportAsyncTask<Void, Void, Void> {
    private Context context;
    private ImageView albumArt;
    private ImageView bg;
    private Bitmap cover;
    private Drawable newDrawable;
    private Drawable[] drawables;
    private Bitmap background;

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetAlbumArt = new HttpGet(StreamServer.Url.CURRENT_ALBUMART);

    public RemoteAlbumArtAsyncGetter(Context context, ImageView albumArt, ImageView bg){
        this.albumArt = albumArt;
        this.bg = bg;
        this.context = context;
        drawables = new Drawable[]{bg.getDrawable(), bg.getDrawable()};
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        try {
            HttpResponse response = httpclient.execute(httpGetAlbumArt);
            InputStream is = response.getEntity().getContent();
            cover = BitmapFactory.decodeStream(is);
            if(cover == null){
                cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_cover);
            }
            background = new StackBlurManager(cover).processNatively(PlaybackFragment.BLUR_RADIUS);
        } catch (IOException e) {
            e.printStackTrace();
            cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_cover);
            background = new StackBlurManager(cover).processNatively(PlaybackFragment.BLUR_RADIUS);
        }

        try{
            newDrawable = new BitmapDrawable(context.getResources(), background);
        }catch (IllegalStateException ignored){}
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        albumArt.setImageBitmap(cover);

        if(newDrawable != null){
            Drawable oldDrawable = bg.getDrawable();

            if(oldDrawable instanceof TransitionDrawable){
                oldDrawable = ((TransitionDrawable) oldDrawable).getDrawable(1);
            }

            drawables[0] = oldDrawable;
            drawables[1] = newDrawable;
            TransitionDrawable transitionDrawable = new TransitionDrawable(drawables);

            bg.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(1000);
        }
//        bg.setImageBitmap(background);
    }

}