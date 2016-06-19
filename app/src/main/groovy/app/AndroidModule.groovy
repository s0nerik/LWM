package app

import android.app.ActivityManager
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
import app.adapters.LocalMusicFragmentsAdapter
import app.adapters.albums.AlbumViewHolder
import app.adapters.albums.AlbumsAdapter
import app.adapters.albums.ArtistAlbumViewHolder
import app.adapters.artists.ArtistViewHolder
import app.adapters.artists.ArtistsAdapter
import app.adapters.songs.SongViewHolder
import app.adapters.songs.SongsListAdapter
import app.adapters.stations.StationViewHolder
import app.events.MainThreadBus
import app.events.RxBus
import app.helpers.CollectionManager
import app.helpers.StationsExplorer
import app.helpers.db.AlbumsCursorGetter
import app.helpers.db.ArtistsCursorGetter
import app.helpers.db.SongsCursorGetter
import app.helpers.wifi.WifiUtils
import app.models.*
import app.players.LocalPlayer
import app.players.StreamPlayer
import app.prefs.MainPrefs
import app.receivers.MediaButtonIntentReceiver
import app.receivers.PendingIntentReceiver
import app.receivers.WiFiDirectBroadcastReceiver
import app.server.HttpStreamServer
import app.server.MusicStation
import app.services.LocalPlayerService
import app.services.MusicStationService
import app.services.StreamPlayerService
import app.ui.Blurer
import app.ui.PaletteApplier
import app.ui.activity.*
import app.ui.custom_view.BroadcastButton
import app.ui.fragment.*
import app.ui.fragment.playback.LocalPlaybackFragment
import app.ui.fragment.playback.RemotePlaybackFragment
import app.ui.notification.NowPlayingNotification
import app.websocket.SocketMessage
import app.websocket.WebSocketMessageClient
import app.websocket.WebSocketMessageServer
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import dagger.Module
import dagger.Provides
import groovy.transform.CompileStatic

import javax.inject.Singleton

import static android.content.Context.*

@Module(injects =
        [
                Daggered,
                MusicCollection,
                PaletteApplier,
                WiFiDirectBroadcastReceiver,
                MusicStation,
                SocketMessage,

                // ViewHolders
                SongViewHolder,
                AlbumViewHolder,
                ArtistAlbumViewHolder,
                ArtistViewHolder,
                StationViewHolder,

                // WebSocket
                WebSocketMessageServer,
                WebSocketMessageClient,


                NowPlayingNotification,
                BroadcastButton,

                // DB Helpers
                AlbumsCursorGetter, SongsCursorGetter, ArtistsCursorGetter,

                // Helpers
                StationsExplorer,

                // Utils
                Utils, WifiUtils, Blurer,

                // Players
                LocalPlayer, StreamPlayer,

                // Servers
                HttpStreamServer,

                // Services
                LocalPlayerService, StreamPlayerService, MusicStationService,

                // Intent receivers
                PendingIntentReceiver, MediaButtonIntentReceiver,

                // Models
                Song, RemoteSong, Album, Artist,

                // Adapters
                SongsListAdapter,
                AlbumsAdapter,
                ArtistsAdapter,
                LocalMusicFragmentsAdapter,

                // Fragments
                QueueFragment,
                NowPlayingFragment,
                SongsListFragment,
                ArtistsListFragment,
                LocalPlaybackFragment,
                RemotePlaybackFragment,
                StationsAroundFragment,
                AlbumsListFragment,
                FindStationsFragment,
                LocalMusicFragment,

                // Activities
                LocalMusicFragment,
                AlbumInfoActivity,
                LocalPlaybackActivity,
                RemotePlaybackActivity,
                ArtistInfoActivity,
                MainActivity,
                StartActivity,

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
        return (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
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
    ActivityManager provideActivityManager() {
        return (ActivityManager) application.getSystemService(ACTIVITY_SERVICE);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new MainThreadBus(ThreadEnforcer.ANY);
    }

    @Provides
    @Singleton
    RxBus provideRxBus() {
        return new RxBus();
    }

    @Provides
    @Singleton
    Blurer provideBlurer() {
        return new Blurer();
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
    MusicStation provideMusicStation() {
        return new MusicStation();
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
        return (ConnectivityManager) application.getSystemService(CONNECTIVITY_SERVICE);
    }

    @Provides
    @Singleton
    WindowManager provideWindowManager() {
        return (WindowManager) application.getSystemService(WINDOW_SERVICE);
    }

    @Provides
    @Singleton
    MainPrefs provideMainPrefs() {
        return MainPrefs.create(application)
    }

    @Provides
    @Singleton
    StationsExplorer provideStationsExplorer() {
        return new StationsExplorer();
    }

    @Provides
    @Singleton
    HttpStreamServer provideHttpStreamServer() {
        return new HttpStreamServer(Config.HTTP_SERVER_PORT)
    }

    @Provides
    @Singleton
    WebSocketMessageServer provideWebSocketMessageServer() {
        return new WebSocketMessageServer(new InetSocketAddress(Config.WS_SERVER_PORT))
    }

    @Provides
    @Singleton
    CollectionManager provideCollectionManager() {
        return new CollectionManager()
    }

}