package app.ui;

import android.app.Activity;

import app.R;
import app.model.chat.ChatMessage;
import app.websocket.entities.ClientInfo;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Croutons {

    public static Crouton clientConnected(Activity activity, ClientInfo clientInfo) {
        String format = activity.getString(R.string.client_connected_format);
        return Crouton.makeText(activity, String.format(format, clientInfo.getName()), Style.CONFIRM);
    }

    public static Crouton clientDisconnected(Activity activity, ClientInfo clientInfo) {
        String format = activity.getString(R.string.client_disconnected_format);
        return Crouton.makeText(activity, String.format(format, clientInfo.getName()), Style.ALERT);
    }

    public static Crouton clientConnected(Activity activity, ClientInfo clientInfo, int resId) {
        String format = activity.getString(R.string.client_connected_format);
        return Crouton.makeText(activity, String.format(format, clientInfo.getName()), Style.CONFIRM, resId);
    }

    public static Crouton clientDisconnected(Activity activity, ClientInfo clientInfo, int resId) {
        String format = activity.getString(R.string.client_disconnected_format);
        return Crouton.makeText(activity, String.format(format, clientInfo.getName()), Style.ALERT, resId);
    }

    public static Crouton messageReceived(Activity activity, ChatMessage chatMessage) {
        String format = activity.getString(R.string.crouton_chat_message_format);
        return Crouton.makeText(activity, String.format(format, chatMessage.getAuthor(), chatMessage.getMessage()), Style.INFO);
    }

    public static Crouton messageReceived(Activity activity, ChatMessage chatMessage, int resId) {
        String format = activity.getString(R.string.crouton_chat_message_format);
        return Crouton.makeText(activity, String.format(format, chatMessage.getAuthor(), chatMessage.getMessage()), Style.INFO, resId);
    }

}
