package com.lwm.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class Utils {

    @Inject
    Resources resources;

    public Utils() {
        Injector.inject(this);
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

    public static int getCurrentSongPosition(LocalPlayer player, List<Song> songList) {
        int pos = -1;
        if (player.hasCurrentSong()) {
            Song song = player.getCurrentSong();
            pos = songList.indexOf(song);
        }
        return pos;
    }

    public static void setSongAsRingtone(Context context, Song song) {
        File newRingtone = new File(song.getSource());

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, newRingtone.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, newRingtone.length());
        values.put(MediaStore.MediaColumns.TITLE, song.getTitle());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.DURATION, song.getDuration());
        values.put(MediaStore.Audio.Media.ARTIST, song.getArtist());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(newRingtone.getAbsolutePath());
        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + newRingtone.getAbsolutePath() + "\"", null);
        Uri newUri = context.getContentResolver().insert(uri, values);

        try {
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
            Toast.makeText(context, String.format(context.getString(R.string.format_ringtone), song.getTitle()), Toast.LENGTH_LONG).show();
        } catch (Throwable t) {}
    }

    public int dpToPixels(int dp) {
        float density = resources.getDisplayMetrics().density;
        return (int) (dp * density);
    }

}
