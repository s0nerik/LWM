package app.modules;

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

import app.App;
import app.PrefManager;
import app.Utils;
import app.adapter.AlbumCoversAdapter;
import app.adapter.AlbumsAdapter;
import app.adapter.ArtistWrappersAdapter;
import app.adapter.LocalMusicFragmentsAdapter;
import app.adapter.SongsListAdapter;
import app.events.MainThreadBus;
import app.helper.bitmap.ArtistAlbumsBitmapHelper;
import app.helper.db.AlbumsCursorGetter;
import app.helper.db.ArtistsCursorGetter;
import app.helper.db.SongsCursorGetter;
import app.helper.wifi.WifiAP;
import app.helper.wifi.WifiUtils;
import app.player.LocalPlayer;
import app.player.StreamPlayer;
import app.receiver.MediaButtonIntentReceiver;
import app.receiver.PendingIntentReceiver;
import app.server.MusicServer;
import app.server.StreamServer;
import app.service.LocalPlayerService;
import app.service.StreamPlayerService;
import app.ui.Blur;
import app.ui.SingleBitmapPaletteInfoCallback;
import app.ui.activity.AlbumInfoActivity;
import app.ui.activity.ArtistInfoActivity;
import app.ui.activity.LocalPlaybackActivity;
import app.ui.activity.MainActivity;
import app.ui.activity.RemotePlaybackActivity;
import app.ui.custom_view.BroadcastButton;
import app.ui.fragment.AlbumsListFragment;
import app.ui.fragment.ArtistsListFragment;
import app.ui.fragment.FindStationsFragment;
import app.ui.fragment.LocalMusicFragment;
import app.ui.fragment.NowPlayingFragment;
import app.ui.fragment.QueueFragment;
import app.ui.fragment.SongsListFragment;
import app.ui.fragment.StationsAroundFragment;
import app.ui.fragment.playback.LocalPlaybackFragment;
import app.ui.fragment.playback.RemotePlaybackFragment;
import app.ui.notification.NowPlayingNotification;
import app.websocket.WebSocketMessageClient;
import app.websocket.WebSocketMessageServer;
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

        // Utils
        Utils.class,
        WifiUtils.class,
        WifiAP.class,
        Blur.class,

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