package app.ui.fragment
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.Nullable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import app.adapter.ChatAdapter
import app.events.chat.ChatMessagesAvailableEvent
import app.events.chat.NotifyMessageAddedEvent
import app.events.chat.ResetUnreadMessagesEvent
import app.events.chat.SendChatMessageEvent
import app.model.chat.ChatMessage
import app.ui.base.DaggerFragment
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.arasthel.swissknife.annotations.OnClick
import com.arasthel.swissknife.annotations.OnEditorAction
import app.R
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
public class ChatFragment extends DaggerFragment {

    @InjectView(R.id.covers)
    RecyclerView mRecyclerView;
    @InjectView(R.id.textField)
    EditText mTextField;

    @Inject
    Bus bus;

    private RecyclerView.Adapter adapter;

    private List<ChatMessage> messages = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        SwissKnife.inject(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.post(new ResetUnreadMessagesEvent());
    }

    @Subscribe
    public void onMessagesAvailable(ChatMessagesAvailableEvent event) {
        messages = event.getMessages();
        adapter = new ChatAdapter(getActivity(), messages);
    }

    @Subscribe
    public void onNotifyMessageAdded(NotifyMessageAddedEvent event) {
        mRecyclerView.getAdapter().notifyItemInserted(messages.size() - 1);
        mRecyclerView.scrollToPosition(messages.size() - 1);
    }

    @OnEditorAction(R.id.textField)
    public boolean onEditorAction(int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            onSend();
            return true;
        }
        return false;
    }

    @OnClick(R.id.btnSend)
    public void onSend() {
        String name = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("client_name", android.os.Build.MODEL);
        String text = mTextField.getText().toString();
        if (!text.isEmpty()) {
            ChatMessage msg = new ChatMessage(name, mTextField.getText().toString());
            bus.post(new SendChatMessageEvent(msg));
            mTextField.setText("");
        }
    }

}
