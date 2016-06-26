package app.di

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
import app.App
import app.Config
import app.Utils
import app.helpers.CollectionManager
import app.helpers.StationsExplorer
import app.helpers.wifi.WifiUtils
import app.players.LocalPlayer
import app.players.StreamPlayer
import app.prefs.MainPrefs
import app.server.HttpStreamServer
import app.server.MusicStation
import app.ui.Blurer
import app.websocket.WebSocketMessageServer
import dagger.Module
import dagger.Provides
import groovy.transform.CompileStatic

import javax.inject.Singleton

import static android.content.Context.*

@Module
@CompileStatic
class AppModule {
    private final App application;

    public AppModule(App application) {
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
        return MainPrefs.get(application)
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