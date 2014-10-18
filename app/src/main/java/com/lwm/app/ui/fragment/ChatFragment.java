package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.ChatAdapter;
import com.lwm.app.events.chat.ChatMessageReceivedEvent;
import com.lwm.app.events.chat.ChatMessagesAvailableEvent;
import com.lwm.app.events.chat.NotifyMessageAddedEvent;
import com.lwm.app.model.chat.ChatMessage;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChatFragment extends Fragment {

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.textField)
    EditText mTextField;

    private List<ChatMessage> messages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getBus().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new ChatAdapter(getActivity(), messages));
    }

    @Override
    public void onDestroy() {
        App.getBus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onMessagesAvailable(ChatMessagesAvailableEvent event) {
        messages = event.getMessages();
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onNotifyMessageAdded(NotifyMessageAddedEvent event) {
        mRecyclerView.getAdapter().notifyItemInserted(messages.size() - 1);
    }

    @OnClick(R.id.btnSend)
    public void onSend(Button btn) {
        ChatMessage msg = new ChatMessage("Me", mTextField.getText().toString());
        App.getBus().post(new ChatMessageReceivedEvent(msg));
        mTextField.setText("");
    }

}
