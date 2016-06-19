package app.ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v7.graphics.Palette
import android.widget.RemoteViews

import app.Injector
import app.R
import app.Utils
import app.models.Song
import app.players.LocalPlayer
import app.receivers.PendingIntentReceiver
import app.ui.PaletteHelper
import app.ui.activity.LocalPlaybackActivity
import groovy.transform.PackageScope

import javax.inject.Inject

class NowPlayingNotification {

    public static final int NOTIFICATION_ID = 505173 // LOL

    public static final String ACTION_CLOSE = "app.player.close"
    public static final String ACTION_PLAY_PAUSE = "app.player.play_pause"
    public static final String ACTION_PREV = "app.player.prev"
    public static final String ACTION_NEXT = "app.player.next"

    public static final int ALBUM_ART_SIZE = 120

    @Inject
    @PackageScope
    LocalPlayer player

    @Inject
    @PackageScope
    ContentResolver contentResolver

    @Inject
    @PackageScope
    Resources resources

    @Inject
    @PackageScope
    Utils utils

    @Inject
    @PackageScope
    Context context

    private Song song

    private final PendingIntent closeIntent
    private final PendingIntent prevIntent
    private final PendingIntent playPauseIntent
    private final PendingIntent nextIntent

    public NowPlayingNotification(Song song) {
        Injector.inject(this)

        closeIntent = PendingIntent.getBroadcast(context, 0, createIntent(context, ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT)
        prevIntent = PendingIntent.getBroadcast(context, 1, createIntent(context, ACTION_PREV), PendingIntent.FLAG_UPDATE_CURRENT)
        playPauseIntent = PendingIntent.getBroadcast(context, 2, createIntent(context, ACTION_PLAY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT)
        nextIntent = PendingIntent.getBroadcast(context, 3, createIntent(context, ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)

        this.song = song
    }

    public Notification create(boolean isPlaying) {

        // Build notification
        def builder = new Notification.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)

//        if (playing) {
//            builder.setSmallIcon(R.drawable.ic_stat_av_play)
//        } else {
//            builder.setSmallIcon(R.drawable.ic_stat_av_pause)
//        }

        builder.smallIcon = R.drawable.ic_notification

        builder.setContentIntent mainPendingIntent
        def notification = builder.build()

        Bitmap cover = getScaledCover()

        // Set default content view
        notification.contentView = getSmallContentView(cover)
        notification.bigContentView = getBigContentView(cover)

        if (isPlaying) {
            notification.contentView.setImageViewResource R.id.btn_play_pause, R.drawable.ic_av_pause
            notification.bigContentView.setImageViewResource R.id.btn_play_pause, R.drawable.ic_av_pause
        } else {
            notification.contentView.setImageViewResource R.id.btn_play_pause, R.drawable.ic_av_play_arrow
            notification.bigContentView.setImageViewResource R.id.btn_play_pause, R.drawable.ic_av_play_arrow
        }

        return notification
    }

    private RemoteViews getBigContentView(Bitmap cover) {
        def bigContentView = new RemoteViews(context.packageName, R.layout.notification_now_playing_big)

        def palette = Palette.generate(cover)
        def swatch = PaletteHelper.getFirstSwatch(palette)

        if (swatch != null) {
            // Set background
            bigContentView.setInt R.id.background, "setBackgroundColor", swatch.rgb

            // Set button colors
            int buttonColor = Utils.stripAlpha(swatch.titleTextColor)

            bigContentView.setInt R.id.btn_prev, "setColorFilter", buttonColor
            bigContentView.setInt R.id.btn_play_pause, "setColorFilter", buttonColor
            bigContentView.setInt R.id.btn_next, "setColorFilter", buttonColor
            bigContentView.setInt R.id.btn_close, "setColorFilter", buttonColor

            // Set text colors
            int textColor = swatch.titleTextColor

            bigContentView.setTextColor R.id.title, textColor
            bigContentView.setTextColor R.id.artist, textColor
            bigContentView.setTextColor R.id.album, textColor
        }

        bigContentView.setTextViewText R.id.title, song.title
        bigContentView.setTextViewText R.id.artist, utils.getArtistName(song.artistName)
        bigContentView.setTextViewText R.id.album, song.albumName

        bigContentView.setImageViewBitmap R.id.album_art, cover

        bigContentView.setOnClickPendingIntent R.id.btn_play_pause, playPauseIntent
        bigContentView.setOnClickPendingIntent R.id.btn_next, nextIntent
        bigContentView.setOnClickPendingIntent R.id.btn_prev, prevIntent
        bigContentView.setOnClickPendingIntent R.id.btn_close, closeIntent

        return bigContentView
    }

    private RemoteViews getSmallContentView(Bitmap cover) {
        def contentView = new RemoteViews(context.packageName, R.layout.notification_now_playing)

        contentView.setImageViewBitmap R.id.album_art, cover

        def palette = Palette.generate(cover)
        def swatch = PaletteHelper.getFirstSwatch(palette)

        if (swatch != null) {
            // Set background
            contentView.setInt R.id.background, "setBackgroundColor", swatch.rgb

            // Set button colors
            int buttonColor = Utils.stripAlpha(swatch.titleTextColor)

            contentView.setInt R.id.btn_play_pause, "setColorFilter", buttonColor
            contentView.setInt R.id.btn_next, "setColorFilter", buttonColor
            contentView.setInt R.id.btn_close, "setColorFilter", buttonColor

            // Set text colors
            int textColor = swatch.titleTextColor

            contentView.setTextColor R.id.title, textColor
            contentView.setTextColor R.id.artist, textColor
            contentView.setTextColor R.id.album, textColor
        }

        contentView.setTextViewText R.id.title, song.title
        contentView.setTextViewText R.id.artist, utils.getArtistName(song.artistName)
        contentView.setTextViewText R.id.album, song.albumName

        contentView.setOnClickPendingIntent R.id.btn_play_pause, playPauseIntent
        contentView.setOnClickPendingIntent R.id.btn_next, nextIntent
        contentView.setOnClickPendingIntent R.id.btn_close, closeIntent

        return contentView
    }

    private Bitmap getScaledCover() {
        Bitmap cover = null
        try {
            def is = contentResolver.openInputStream(song.albumArtUri)
            is.withStream {
                cover = BitmapFactory.decodeStream it
            }
        } catch (ignore) {}

        cover = cover ?: utils.noCoverBitmap

        // Scale down bitmap not to get binder error
        return scaleDownBitmap(cover, ALBUM_ART_SIZE, resources)
    }

    private PendingIntent getMainPendingIntent() {
        Intent intent = new Intent(context, LocalPlaybackActivity)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack LocalPlaybackActivity
        stackBuilder.addNextIntent intent

        Bundle extras = new Bundle()
        extras.putBoolean "from_notification", true

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT, extras)
    }

    private static Intent createIntent(Context context, String action) {
        Intent intent = new Intent(context, PendingIntentReceiver)
        intent.action = action
        return intent
    }

    private static Bitmap scaleDownBitmap(Bitmap bitmap, int newHeight, Resources res) {

        final float densityMultiplier = res.displayMetrics.density

        int h = newHeight * densityMultiplier as int
        int w = h * bitmap.width / (double) bitmap.height as int

        bitmap = Bitmap.createScaledBitmap bitmap, w, h, true

        return bitmap
    }

}
