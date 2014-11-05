package com.lwm.app.modules;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;

import com.lwm.app.App;
import com.lwm.app.events.MainThreadBus;
import com.lwm.app.lib.WifiAP;
import com.lwm.app.player.LocalPlayer;
import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.MusicServer;
import com.lwm.app.service.LocalPlayerService;
import com.lwm.app.ui.activity.AlbumInfoActivity;
import com.lwm.app.ui.activity.LocalPlaybackActivity;
import com.lwm.app.ui.activity.LocalSongChooserActivity;
import com.lwm.app.ui.activity.StationChooserActivity;
import com.lwm.app.ui.async.LocalQueueLoader;
import com.lwm.app.ui.fragment.LocalPlaybackFragment;
import com.lwm.app.ui.fragment.NowPlayingFragment;
import com.lwm.app.ui.fragment.PlayersAroundFragment;
import com.lwm.app.ui.fragment.QueueFragment;
import com.lwm.app.ui.fragment.SongsListFragment;
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
        WifiAP.class,

        LocalPlayer.class,

        LocalPlayerService.class,

        NowPlayingFragment.class,
        SongsListFragment.class,

        LocalPlaybackFragment.class,

        QueueFragment.class,
        LocalQueueLoader.class,

        PlayersAroundFragment.class,

        LocalSongChooserActivity.class,

        AlbumInfoActivity.class,

        LocalPlaybackActivity.class,

        StationChooserActivity.class

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
    MusicServer provideMusicServer() {
        return new MusicServer();
    }

}