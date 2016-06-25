package app.ui.activity

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import app.App
import app.R
import app.events.chat.ChatMessageReceivedEvent
import app.events.chat.SetUnreadMessagesEvent
import app.events.client.SocketClosedEvent
import app.models.Song
import app.players.StreamPlayer
import app.services.StreamPlayerService
import app.ui.fragment.playback.RemotePlaybackFragment
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

import javax.inject.Inject

@CompileStatic
class RemotePlaybackActivity extends PlaybackActivity {

    private int duration;
    private String durationString;
    private String title;
    private String artist;
    private String album;
    private RemotePlaybackFragment playbackFragment;

    private int unreadMessagesCount = 0;
    private MenuItem chatButton;
    private TextView newMessagesCounter;

    @Inject
    protected Bus bus

    @Inject
    protected StreamPlayer player

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        playbackFragment = (RemotePlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().inject(this)
        Debug.d("RemotePlaybackActivity.onCreate()");
        setContentView(R.layout.activity_remote_playback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        Song song = player.currentSong
        if (song != null) {
//            setSongInfo(song);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
//        Croutons.messageReceived(this, event.getMessage(), R.id.albumArtLayout).show();
        unreadMessagesCount += 1;
        newMessagesCounter.setVisibility(View.VISIBLE);
        newMessagesCounter.setText(String.valueOf(unreadMessagesCount < 10 ? unreadMessagesCount : "+"));
    }

    @Subscribe
    public void setUnreadMessagesCount(SetUnreadMessagesEvent event) {
        unreadMessagesCount = event.getCount();
        if (newMessagesCounter != null) {
            if (unreadMessagesCount > 0) {
                newMessagesCounter.setVisibility(View.VISIBLE);
                newMessagesCounter.setText(String.valueOf(unreadMessagesCount < 10 ? unreadMessagesCount : "+"));
            } else {
                newMessagesCounter.setVisibility(View.GONE);
            }
        }
    }

    @Subscribe
    public void onSocketClosed(SocketClosedEvent event) {
        String title = "Station stopped broadcasting";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    void onClick(DialogInterface dialogInterface, int i) {
                        finish()
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        if(player.isPlaying()) {
            player.stop();
        }
        stopService intent(StreamPlayerService)
        super.onDestroy();
    }
}