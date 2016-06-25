package app.di


import app.Utils
import app.adapters.LocalMusicFragmentsAdapter
import app.adapters.albums.AlbumViewHolder
import app.adapters.albums.AlbumsAdapter
import app.adapters.albums.ArtistAlbumViewHolder
import app.adapters.artists.ArtistViewHolder
import app.adapters.artists.ArtistsAdapter
import app.adapters.songs.SongViewHolder
import app.adapters.songs.SongsListAdapter
import app.adapters.stations.StationViewHolder
import app.helpers.StationsExplorer
import app.helpers.db.AlbumsCursorGetter
import app.helpers.db.ArtistsCursorGetter
import app.helpers.db.SongsCursorGetter
import app.helpers.wifi.WifiUtils
import app.models.Album
import app.models.Artist
import app.models.MusicCollection
import app.models.RemoteSong
import app.models.Song
import app.players.LocalPlayer
import app.players.StreamPlayer
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
import app.ui.activity.AlbumInfoActivity
import app.ui.activity.ArtistInfoActivity
import app.ui.activity.LocalPlaybackActivity
import app.ui.activity.MainActivity
import app.ui.activity.RemotePlaybackActivity
import app.ui.activity.StartActivity
import app.ui.custom_view.BroadcastButton
import app.ui.fragment.AlbumsListFragment
import app.ui.fragment.ArtistsListFragment
import app.ui.fragment.FindStationsFragment
import app.ui.fragment.LocalMusicFragment
import app.ui.fragment.NowPlayingFragment
import app.ui.fragment.QueueFragment
import app.ui.fragment.SongsListFragment
import app.ui.fragment.StationsAroundFragment
import app.ui.fragment.playback.LocalPlaybackFragment
import app.ui.fragment.playback.RemotePlaybackFragment
import app.ui.notification.NowPlayingNotification
import app.websocket.SocketMessage
import app.websocket.WebSocketMessageClient
import app.websocket.WebSocketMessageServer
import dagger.Component

import javax.inject.Singleton

@Singleton
@Component(modules = AppModule)
interface AppComponent {
    // Generic
    void inject(MusicCollection x)
    void inject(PaletteApplier x)
    void inject(WiFiDirectBroadcastReceiver x)
    void inject(MusicStation x)
    void inject(SocketMessage x)

    // ViewHolders
    void inject(SongViewHolder x)
    void inject(AlbumViewHolder x)
    void inject(ArtistAlbumViewHolder x)
    void inject(ArtistViewHolder x)
    void inject(StationViewHolder x)

    // WebSocket
    void inject(WebSocketMessageServer x)
    void inject(WebSocketMessageClient x)
    void inject(NowPlayingNotification x)
    void inject(BroadcastButton x)

    // DB Helpers
    void inject(AlbumsCursorGetter x)
    void inject(SongsCursorGetter x)
    void inject(ArtistsCursorGetter x)

    // Helpers
    void inject(StationsExplorer x)

    // Utils
    void inject(Utils x)
    void inject(WifiUtils x)
    void inject(Blurer x)

    // Players
    void inject(LocalPlayer x)
    void inject(StreamPlayer x)

    // Servers
    void inject(HttpStreamServer x)

    // Services
    void inject(LocalPlayerService x)
    void inject(StreamPlayerService x)
    void inject(MusicStationService x)

    // Intent receivers
    void inject(PendingIntentReceiver x)
    void inject(MediaButtonIntentReceiver x)

    // Models
    void inject(Song x)
    void inject(RemoteSong x)
    void inject(Album x)
    void inject(Artist x)

    // Adapters
    void inject(SongsListAdapter x)
    void inject(AlbumsAdapter x)
    void inject(ArtistsAdapter x)
    void inject(LocalMusicFragmentsAdapter x)

    // Fragments
    void inject(QueueFragment x)
    void inject(NowPlayingFragment x)
    void inject(SongsListFragment x)
    void inject(ArtistsListFragment x)
    void inject(LocalPlaybackFragment x)
    void inject(RemotePlaybackFragment x)
    void inject(StationsAroundFragment x)
    void inject(AlbumsListFragment x)
    void inject(FindStationsFragment x)
    void inject(LocalMusicFragment x)

    // Activities
    void inject(AlbumInfoActivity x)
    void inject(LocalPlaybackActivity x)
    void inject(RemotePlaybackActivity x)
    void inject(ArtistInfoActivity x)
    void inject(MainActivity x)
    void inject(StartActivity x)
}
