package app

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import app.model.Song
import app.player.LocalPlayer
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import ru.noties.debug.Debug

import javax.inject.Inject

import static java.lang.reflect.Modifier.FINAL
import static java.lang.reflect.Modifier.PUBLIC
import static java.lang.reflect.Modifier.STATIC

@CompileStatic
@PackageScope(PackageScopeTarget.FIELDS)
class Utils extends Daggered {

    @Inject
    Resources resources

    @Inject
    WindowManager windowManager

    @Inject
    ActivityManager activityManager

    @Inject
    ContentResolver contentResolver

    public String getArtistName(String name) {
        if ("<unknown>".equals(name)) {
            return resources.getString(R.string.unknown_artist);
        } else {
            return name;
        }
    }

    public String getRealImagePathFromUri(Uri contentUri) {
        Cursor cursor = null
        try {
            String[] proj = [MediaStore.Images.Media.DATA]
            cursor = contentResolver.query contentUri, proj, null, null, null
            int column_index = cursor.getColumnIndexOrThrow MediaStore.Images.Media.DATA
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } catch(Exception ignored) {
            return null
        } finally {
            cursor?.close()
        }
    }

    Uri resourceToUri (int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(resID) + '/' +
                resources.getResourceTypeName(resID) + '/' +
                resources.getResourceEntryName(resID) );
    }

    public Bitmap getNoCoverBitmap() {
        BitmapFactory.decodeResource(resources, R.drawable.no_cover)
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
        return player.currentSong ? songList.indexOf(player.currentSong) : -1
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

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    static Drawable copyDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            def bm1 = (drawable as BitmapDrawable).getBitmap()
            return new BitmapDrawable(bm1)
        }
        if (drawable instanceof TransitionDrawable) {
            def bm1 = (drawable as TransitionDrawable).getDrawable(1)
            return bm1
        }
        return drawable
    }

    boolean isServiceRunning(Class<?> serviceClass) {
        activityManager.getRunningServices(Integer.MAX_VALUE).each { ActivityManager.RunningServiceInfo service ->
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    public static <T> T fromJson(String json) {
        new JsonSlurper().parseText(json) as T
    }

    public static String toJson(Object obj) {
        JsonOutput.toJson(obj)
    }

    public static String getConstantName(Class c, Object constant) {
        c.declaredFields.find {
            it.modifiers == (PUBLIC | STATIC | FINAL) && it.get(null) == constant
        }.name
    }

}
