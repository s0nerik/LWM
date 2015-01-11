package com.lwm.app.modules;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.lwm.app.App;
import com.lwm.app.PrefManager;
import com.lwm.app.Utils;
import com.lwm.app.adapter.AlbumCoversAdapter;
import com.lwm.app.adapter.AlbumsAdapter;
import com.lwm.app.adapter.ArtistWrappersAdapter;
import com.lwm.app.adapter.LocalMusicFragmentsAdapter;
import com.lwm.app.adapter.SongsListAdapter;
import com.lwm.app.events.MainThreadBus;
import com.lwm.app.helper.bitmap.ArtistAlbumsBitmapHelper;
import com.lwm.app.helper.db.AlbumsCursorGetter;
import com.lwm.app.helper.db.ArtistsCursorGetter;
import com.lwm.app.helper.db.SongsCursorGetter;
import com.lwm.app.helper.wifi.WifiAP;
import com.lwm.app.helper.wifi.WifiUtils;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.receiver.MediaButtonIntentReceiver;
import com.lwm.app.receiver.PendingIntentReceiver;
import com.lwm.app.server.MusicServer;
import com.lwm.app.server.StreamServer;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.service.StreamPlayerService;
import com.lwm.app.ui.SingleBitmapPaletteInfoCallback;
import com.lwm.app.ui.activity.AlbumInfoActivity;
import com.lwm.app.ui.activity.ArtistInfoActivity;
import com.lwm.app.ui.activity.LocalPlaybackActivity;
import com.lwm.app.ui.activity.MainActivity;
import com.lwm.app.ui.fragment.LocalMusicFragment;
import com.lwm.app.ui.activity.RemotePlaybackActivity;
import com.lwm.app.ui.async.AlbumsLoaderTask;
import com.lwm.app.ui.async.ArtistsLoaderTask;
import com.lwm.app.ui.async.QueueLoaderTask;
import com.lwm.app.ui.async.SongsLoaderTask;
import com.lwm.app.ui.custom_view.BroadcastButton;
import com.lwm.app.ui.fragment.AlbumsListFragment;
import com.lwm.app.ui.fragment.ArtistsListFragment;
import com.lwm.app.ui.fragment.FindStationsFragment;
import com.lwm.app.ui.fragment.NowPlayingFragment;
import com.lwm.app.ui.fragment.StationsAroundFragment;
import com.lwm.app.ui.fragment.QueueFragment;
import com.lwm.app.ui.fragment.SongsListFragment;
import com.lwm.app.ui.fragment.playback.LocalPlaybackFragment;
import com.lwm.app.ui.fragment.playback.RemotePlaybackFragment;
import com.lwm.app.ui.notification.NowPlayingNotification;
import com.lwm.app.websocket.WebSocketMessageClient;
import com.lwm.app.websocket.WebSocketMessageServer;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;

@Module(injects = {
        ArtistAlbumsBitmapHelper.class,

        WebSocketMessageServer.class,
        WebSocketMessageClient.class,

        SingleBitmapPaletteInfoCallback.class,
        AlbumCoversAdapter.BitmapPaletteInfoCallback.class,

        NowPlayingNotification.class,

        BroadcastButton.class,

        // DB Helpers
        AlbumsCursorGetter.class,
        SongsCursorGetter.class,
        ArtistsCursorGetter.class,

        // AsyncTasks
        SongsLoaderTask.class,
        QueueLoaderTask.class,
        ArtistsLoaderTask.class,
        AlbumsLoaderTask.class,

        // Utils
        Utils.class,
        WifiUtils.class,
        WifiAP.class,

        // Players
        LocalPlayer.class,
        StreamPlayer.class,

        // Servers
        StreamServer.class,
        MusicServer.class,

        // Playback services
        LocalPlayerService.class,
        StreamPlayerService.class,

        // Intent receivers
        PendingIntentReceiver.class,
        MediaButtonIntentReceiver.class,

        // Adapters
        SongsListAdapter.class,
        AlbumsAdapter.class,
        ArtistWrappersAdapter.class,
        AlbumCoversAdapter.class,
        LocalMusicFragmentsAdapter.class,

        // Fragments
        QueueFragment.class,
        NowPlayingFragment.class,
        SongsListFragment.class,
        ArtistsListFragment.class,
        LocalPlaybackFragment.class,
        RemotePlaybackFragment.class,
        StationsAroundFragment.class,
        AlbumsListFragment.class,
        FindStationsFragment.class,
        LocalMusicFragment.class,

        // Activities
        LocalMusicFragment.class,
        AlbumInfoActivity.class,
        LocalPlaybackActivity.class,
        RemotePlaybackActivity.class,
        ArtistInfoActivity.class,
        MainActivity.class,

        },
        library = true)
public class AndroidModule {
    private final App application;

    public AndroidModule(App application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    WifiManager provideWifiManager() {
        return (WifiManager) application.getSystemService(WIFI_SERVICE);
    }

    @Provides
    @Singleton
    LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) application.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    @Singleton
    AudioManager provideAudioManager() {
        return (AudioManager) application.getSystemService(AUDIO_SERVICE);
    }

    @Provides
    @Singleton
    NotificationManager provideNotificationManager() {
        return (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return application.getResources();
    }

    @Provides
    @Singleton
    ContentResolver provideContentResolver() {
        return application.getContentResolver();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return application.getSharedPreferences("", MODE_PRIVATE);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new MainThreadBus(ThreadEnforcer.ANY);
    }

    @Provides
    @Singleton
    LocalPlayer provideLocalPlayer() {
        return new LocalPlayer();
    }

    @Provides
    @Singleton
    StreamPlayer provideStreamPlayer() {
        return new StreamPlayer();
    }

    @Provides
    @Singleton
    WifiAP provideWifiAP() {
        return new WifiAP();
    }

    @Provides
    @Singleton
    Utils provideUtils() {
        return new Utils();
    }

    @Provides
    @Singleton
    WifiUtils provideWifiUtils() {
        return new WifiUtils();
    }

    @Provides
    @Singleton
    ConnectivityManager provideConnectivityManager() {
        return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    @Singleton
    WindowManager provideWindowManager() {
        return (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
    }

    @Provides
    @Singleton
    PrefManager providePrefManager() {
        return new PrefManager(application);
    }

}