package com.lwm.app.ui.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.graphics.Palette;
import android.widget.RemoteViews;

import com.lwm.app.Injector;
import com.lwm.app.R;
import com.lwm.app.Utils;
import com.lwm.app.model.Song;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.receiver.PendingIntentReceiver;
import com.lwm.app.ui.PaletteHelper;
import com.lwm.app.ui.activity.LocalPlaybackActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.inject.Inject;

public class NowPlayingNotification {

    public static final int NOTIFICATION_ID = 505173; // LOL

    public static final String ACTION_CLOSE = "com.lwm.app.player.close";
    public static final String ACTION_PLAY_PAUSE = "com.lwm.app.player.play_pause";
    public static final String ACTION_PREV = "com.lwm.app.player.prev";
    public static final String ACTION_NEXT = "com.lwm.app.player.next";

    public static final int ALBUM_ART_SIZE = 120;

    @Inject
    LocalPlayer player;

    @Inject
    ContentResolver contentResolver;

    @Inject
    Resources resources;

    @Inject
    Utils utils;

    @Inject
    Context context;

    private Song song;

    private final PendingIntent closeIntent;
    private final PendingIntent prevIntent;
    private final PendingIntent playPauseIntent;
    private final PendingIntent nextIntent;

    public NowPlayingNotification(Song song) {
        Injector.inject(this);

        closeIntent = PendingIntent.getBroadcast(context, 0, createIntent(context, ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
        prevIntent = PendingIntent.getBroadcast(context, 1, createIntent(context, ACTION_PREV), PendingIntent.FLAG_UPDATE_CURRENT);
        playPauseIntent = PendingIntent.getBroadcast(context, 2, createIntent(context, ACTION_PLAY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        nextIntent = PendingIntent.getBroadcast(context, 3, createIntent(context, ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);

        this.song = song;
    }

    public Notification create(boolean isPlaying){

        // Build notification
        Notification.Builder builder = new Notification.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);

//        if (isPlaying) {
//            builder.setSmallIcon(R.drawable.ic_stat_av_play);
//        } else {
//            builder.setSmallIcon(R.drawable.ic_stat_av_pause);
//        }

        builder.setSmallIcon(R.drawable.ic_notification);

        builder.setContentIntent(getMainPendingIntent());
        Notification notification = builder.build();

        Bitmap cover = getScaledCover();

        // Set default content view
        notification.contentView = getSmallContentView(cover);
        notification.bigContentView = getBigContentView(cover);

        if (isPlaying) {
            notification.contentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_av_pause);
            notification.bigContentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_av_pause);
        } else {
            notification.contentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_av_play_arrow);
            notification.bigContentView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_av_play_arrow);
        }

        return notification;
    }

    private RemoteViews getBigContentView(Bitmap cover) {
        RemoteViews bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_now_playing_big);

        Palette palette = Palette.generate(cover);
        Palette.Swatch swatch = PaletteHelper.getFirstSwatch(palette);

        if (swatch != null) {
            // Set background
            bigContentView.setInt(R.id.background, "setBackgroundColor", swatch.getRgb());

            // Set button colors
            int buttonColor = Utils.stripAlpha(swatch.getTitleTextColor());

            bigContentView.setInt(R.id.btn_prev, "setColorFilter", buttonColor);
            bigContentView.setInt(R.id.btn_play_pause, "setColorFilter", buttonColor);
            bigContentView.setInt(R.id.btn_next, "setColorFilter", buttonColor);
            bigContentView.setInt(R.id.btn_close, "setColorFilter", buttonColor);

            // Set text colors
            bigContentView.setTextColor(R.id.title, swatch.getTitleTextColor());
            bigContentView.setTextColor(R.id.artist, swatch.getBodyTextColor());
            bigContentView.setTextColor(R.id.album, swatch.getBodyTextColor());
        }

        bigContentView.setTextViewText(R.id.title, song.getTitle());
        bigContentView.setTextViewText(R.id.artist, utils.getArtistName(song.getArtist()));
        bigContentView.setTextViewText(R.id.album, song.getAlbum());

        bigContentView.setImageViewBitmap(R.id.album_art, cover);

        bigContentView.setOnClickPendingIntent(R.id.btn_play_pause, playPauseIntent);
        bigContentView.setOnClickPendingIntent(R.id.btn_next, nextIntent);
        bigContentView.setOnClickPendingIntent(R.id.btn_prev, prevIntent);
        bigContentView.setOnClickPendingIntent(R.id.btn_close, closeIntent);

        return bigContentView;
    }

    private RemoteViews getSmallContentView(Bitmap cover) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_now_playing);

        contentView.setImageViewBitmap(R.id.album_art, cover);

        Palette palette = Palette.generate(cover);
        Palette.Swatch swatch = PaletteHelper.getFirstSwatch(palette);

        if (swatch != null) {
            // Set background
            contentView.setInt(R.id.background, "setBackgroundColor", swatch.getRgb());

            // Set button colors
            int buttonColor = Utils.stripAlpha(swatch.getTitleTextColor());

            contentView.setInt(R.id.btn_play_pause, "setColorFilter", buttonColor);
            contentView.setInt(R.id.btn_next, "setColorFilter", buttonColor);
            contentView.setInt(R.id.btn_close, "setColorFilter", buttonColor);

            // Set text colors
            contentView.setTextColor(R.id.title, swatch.getTitleTextColor());
            contentView.setTextColor(R.id.artist, swatch.getBodyTextColor());
            contentView.setTextColor(R.id.album, swatch.getBodyTextColor());
        }

        contentView.setTextViewText(R.id.title, song.getTitle());
        contentView.setTextViewText(R.id.artist, utils.getArtistName(song.getArtist()));
        contentView.setTextViewText(R.id.album, song.getAlbum());

        contentView.setOnClickPendingIntent(R.id.btn_play_pause, playPauseIntent);
        contentView.setOnClickPendingIntent(R.id.btn_next, nextIntent);
        contentView.setOnClickPendingIntent(R.id.btn_close, closeIntent);

        return contentView;
    }

    private Bitmap getScaledCover() {
        // Get album art bitmap, if not exists, use default resource
        Bitmap cover;
        try {
            InputStream is = contentResolver.openInputStream(song.getAlbumArtUri());
            cover = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            cover = BitmapFactory.decodeResource(resources, R.drawable.no_cover);
        }

        // Scale down bitmap not to get binder error
        return scaleDownBitmap(cover, ALBUM_ART_SIZE, resources);
    }

    private PendingIntent getMainPendingIntent() {
        Intent intent = new Intent(context, LocalPlaybackActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LocalPlaybackActivity.class);
        stackBuilder.addNextIntent(intent);

        Bundle extras = new Bundle();
        extras.putBoolean("from_notification", true);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT, extras);
    }

    private Intent createIntent(Context context, String action) {
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
