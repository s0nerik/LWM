package com.lwm.app;

import android.content.Context;
import android.content.res.Resources;

public class Utils {

    private Resources resources;

    public Utils(Context context) {
        resources = context.getResources();
    }

    public String getArtistName(String name) {
        if ("<unknown>".equals(name)) {
            return resources.getString(R.string.unknown_artist);
        } else {
            return name;
        }
    }

}
