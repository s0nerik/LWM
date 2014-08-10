package com.lwm.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    private Context context;
    private Resources resources;

    public Utils(Context context) {
        this.context = context;
        resources = context.getResources();
    }

    public String getArtistName(String name) {
        if ("<unknown>".equals(name)) {
            return resources.getString(R.string.unknown_artist);
        } else {
            return name;
        }
    }

    public static void setAlbumArtFromUri(Context context, ImageView view, Uri uri) {
        InputStream is = null;
        try {
            if (uri != null) {
                is = context.getContentResolver().openInputStream(uri);
                Drawable d = Drawable.createFromStream(is, null);
                view.setImageDrawable(d);
            }
        } catch (FileNotFoundException e) {
            view.setImageResource(R.drawable.no_cover);
        } catch(Exception e) {
            Log.e(App.TAG, "Unable to set ImageView from URI: " + e.toString());
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException ignored) {}
        }
    }

}
