package app;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import app.R;

import app.model.Song;
import app.player.LocalPlayer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import ru.noties.debug.Debug;

public class Utils {

    @Inject
    Resources resources;

    @Inject
    WindowManager windowManager;

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
            Debug.d("Unable to set ImageView from URI: %s", e);
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

    public int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL   = 16;
        final byte GREEN_CHANNEL =  8;
        final byte BLUE_CHANNEL  =  0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int)(((float)(color1 >> ALPHA_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> ALPHA_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int r = ((int)(((float)(color1 >> RED_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> RED_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int g = ((int)(((float)(color1 >> GREEN_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> GREEN_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int b = ((int)(((float)(color1 & 0xff )*amount) +
                ((float)(color2 & 0xff )*inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int darkerColor (int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        color = Color.HSVToColor(hsv);

        return color;
    }

    public static int stripAlpha(int color) {
        return color | 0xFF000000;
    }

}
