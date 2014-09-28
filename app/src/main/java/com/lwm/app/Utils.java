package com.lwm.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Random;

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
        try {
            if (uri != null) {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                if (bmp != null)
                    view.setImageBitmap(bmp);
                else
                    view.setImageResource(R.drawable.no_cover);
            } else {
                view.setImageResource(R.drawable.no_cover);
            }
        } catch (IOException e) {
            Log.d(App.TAG, "Unable to set ImageView from URI: " + e.toString());
            view.setImageResource(R.drawable.no_cover);
        }
    }

    public static int getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);

        return Color.rgb(r, g, b);
    }

    public static String getRandomColorString() {
        return String.format("#%06X", 0xFFFFFF & getRandomColor());
    }

}
