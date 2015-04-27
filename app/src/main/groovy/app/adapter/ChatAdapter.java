package app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lwm.app.R;
import app.model.chat.ChatMessage;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatMessage> messages;
    private Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(context, R.layout.item_chat, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ChatMessage message = messages.get(i);
        viewHolder.mMessage.setText(message.getMessage());
        viewHolder.mAuthor.setText(message.getAuthor()+":");
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_chat.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.author)
        TextView mAuthor;
        @InjectView(R.id.message)
        TextView mMessage;

        ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
