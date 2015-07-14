package app.modules
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import app.App
import app.Daggered
import app.PrefManager
import app.Utils
import app.adapter.*
import app.adapter.view_holders.ArtistViewHolder
import app.adapter.view_holders.OnContextMenuItemClickListener
import app.adapter.view_holders.SongViewHolder
import app.adapter.view_holders.WifiP2pDeviceViewHolder
import app.data_managers.AlbumsManager
import app.data_managers.ArtistsManager
import app.data_managers.SongsManager
import app.events.MainThreadBus
import app.helper.db.AlbumsCursorGetter
import app.helper.db.ArtistsCursorGetter
import app.helper.db.SongsCursorGetter
import app.helper.wifi.WifiAP
import app.helper.wifi.WifiUtils
import app.player.LocalPlayer
import app.player.StreamPlayer
import app.receiver.MediaButtonIntentReceiver
import app.receiver.PendingIntentReceiver
import app.receiver.WiFiDirectBroadcastReceiver
import app.server.MusicServer
import app.server.StreamServer
import app.service.LocalPlayerService
import app.service.StreamPlayerService
import app.ui.Blur
import app.ui.PaletteApplier
import app.ui.activity.*
import app.ui.custom_view.BroadcastButton
import app.ui.fragment.*
import app.ui.fragment.playback.LocalPlaybackFragment
import app.ui.fragment.playback.RemotePlaybackFragment
import app.ui.notification.NowPlayingNotification
import app.websocket.WebSocketMessageClient
import app.websocket.WebSocketMessageServer
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import dagger.Module
import dagger.Provides
import groovy.transform.CompileStatic

import javax.inject.Singleton

import static android.content.Context.*

@Module(injects = [
        Daggered,
        PaletteApplier,
        WiFiP2pDevicesAdapter,
        WiFiDirectBroadcastReceiver,
        WifiP2pDeviceViewHolder,


        SongViewHolder.class,
        ArtistViewHolder.class,
        OnContextMenuItemClickListener.class,

//        ArtistAlbumsBitmapHelper.class,

        WebSocketMessageServer.class,
        WebSocketMessageClient.class,

//        AlbumCoversAdapter.BitmapPaletteInfoCallback.class,

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

        ],
        library = true)
@CompileStatic
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
    WifiP2pManager provideWifiP2pManager() {
        return (WifiP2pManager) application.getSystemService(WIFI_P2P_SERVICE);
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
    Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
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

    @Provides
    @Singleton
    SongsManager provideSongsManager() {
        return new SongsManager();
    }

    @Provides
    @Singleton
    AlbumsManager provideAlbumsManager() {
        return new AlbumsManager();
    }

    @Provides
    @Singleton
    ArtistsManager provideArtistsManager() {
        return new ArtistsManager();
    }

}