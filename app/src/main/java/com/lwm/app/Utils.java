package com.lwm.app;

import android.content.Context;
import android.content.res.Resources;

public class Utils {

    private static Resources resources;

    Utils(Context context) {
        resources = context.getResources();
    }

    public static String getArtistName(String name) {
        if ("<unknown>".equals(name)) {
            return resources.getString(R.string.unknown_artist);
        } else {
            return name;
        }
    }

}
