package com.lwm.app.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.lwm.app.App;
import com.lwm.app.R;
import com.lwm.app.adapter.ChatAdapter;
import com.lwm.app.events.chat.ChatMessagesAvailableEvent;
import com.lwm.app.events.chat.NotifyMessageAddedEvent;
import com.lwm.app.events.chat.ResetUnreadMessagesEvent;
import com.lwm.app.events.chat.SendChatMessageEvent;
import com.lwm.app.model.chat.ChatMessage;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class ChatFragment extends Fragment {

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.textField)
    EditText mTextField;

    private RecyclerView.Adapter adapter;

    private List<ChatMessage> messages = new ArrayList<>();

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
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        App.getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getBus().post(new ResetUnreadMessagesEvent());
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
        ChatMessage msg = new ChatMessage(name, mTextField.getText().toString());
        App.getBus().post(new SendChatMessageEvent(msg));
        mTextField.setText("");
    }

}
