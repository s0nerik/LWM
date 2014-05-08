package com.lwm.app.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.ui.activity.LocalPlaybackActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class NowPlayingNotification {

    public static final int NOTIFICATION_NOW_PLAYING_ID = 0;

    public static final String ACTION_CLOSE = "com.lwm.app.player.close";
    public static final String ACTION_PLAY_PAUSE = "com.lwm.app.player.play_pause";
    public static final String ACTION_PREV = "com.lwm.app.player.prev";
    public static final String ACTION_NEXT = "com.lwm.app.player.next";

    public static final int ALBUM_ART_SIZE = 120;

    private Context context;
    private static NotificationManager notificationManager;

    public NowPlayingNotification(Context context){
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void hide(){
        if(notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_NOW_PLAYING_ID);
        }
        if(App.localPlayerActive()){
            App.getLocalPlayer().setUpdateNotification(false);
        }
    }

    public void show(){

        boolean isPlaying;
        Song currentSong;
        if(App.localPlayerActive() && App.getLocalPlayer().hasCurrentSong()){
            LocalPlayer player = App.getLocalPlayer();
            currentSong = player.getCurrentSong();
            isPlaying = player.isPlaying();
            player.setUpdateNotification(true);
        }else{
            return;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Resources resources = context.getResources();

        // Get album art bitmap, if not exists, use default resource
        Bitmap cover;
        try {
            InputStream is = contentResolver.openInputStream(currentSong.getAlbumArtUri());
            cover = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            cover = BitmapFactory.decodeResource(resources, R.drawable.no_cover);
        }

        // Scale down bitmap not to get binder error
        cover = scaleDownBitmap(cover, ALBUM_ART_SIZE, resources);

        // Default content view
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_now_playing);
        contentView.setImageViewBitmap(R.id.album_art, cover);
        contentView.setTextViewText(R.id.title, currentSong.getTitle());
        contentView.setTextViewText(R.id.artist, currentSong.getArtist());

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setTicker(currentSong.getArtist() + " - " + currentSong.getTitle())
                .setOngoing(true);

        if(isPlaying){
            builder.setSmallIcon(R.drawable.ic_stat_av_play);
        }else{
            builder.setSmallIcon(R.drawable.ic_stat_av_pause);
        }

        // Build a backStack
        Intent intent = new Intent(context, LocalPlaybackActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();

        // Set default content view
        notification.contentView = contentView;

        PendingIntent closeIntent = PendingIntent.getBroadcast(context, 0, createIntent(ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent prevIntent = PendingIntent.getBroadcast(context, 1, createIntent(ACTION_PREV), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(context, 2, createIntent(ACTION_PLAY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextIntent = PendingIntent.getBroadcast(context, 3, createIntent(ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);

        // If Android >= 3.0, add PendingIntent's to default contentView and make a big one
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            contentView.setOnClickPendingIntent(R.id.btn_close, closeIntent);
            contentView.setOnClickPendingIntent(R.id.btn_play_pause, playPauseIntent);
            contentView.setOnClickPendingIntent(R.id.btn_next, nextIntent);

            RemoteViews bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_now_playing_big);
            bigContentView.setTextViewText(R.id.title, currentSong.getTitle());
            bigContentView.setTextViewText(R.id.artist, currentSong.getArtist());
            bigContentView.setImageViewBitmap(R.id.album_art, cover);

            bigContentView.setOnClickPendingIntent(R.id.btn_close, closeIntent);
            bigContentView.setOnClickPendingIntent(R.id.btn_play_pause, playPauseIntent);
            bigContentView.setOnClickPendingIntent(R.id.btn_next, nextIntent);
            bigContentView.setOnClickPendingIntent(R.id.btn_prev, prevIntent);

            if(isPlaying){
                contentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_notification_pause);
                bigContentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_notification_pause);
            }else{
                contentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_notification_play);
                bigContentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_notification_play);
            }

            // Set big content view
            notification.bigContentView = bigContentView;
        }

        notificationManager.notify(NOTIFICATION_NOW_PLAYING_ID, notification);

    }

    private Intent createIntent(String action) {
        Intent intent = new Intent(context, PendingIntentReceiver.class);
        intent.setAction(action);
        return intent;
    }

    private Bitmap scaleDownBitmap(Bitmap bitmap, int newHeight, Resources res) {

        final float densityMultiplier = res.getDisplayMetrics().density;

        int h = (int) (newHeight*densityMultiplier);
        int w = (int) (h * bitmap.getWidth()/((double) bitmap.getHeight()));

        bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);

        return bitmap;
    }

}
