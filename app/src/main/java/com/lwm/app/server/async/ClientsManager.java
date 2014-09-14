package com.lwm.app.server.async;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.lwm.app.App;
import com.lwm.app.model.Client;
import com.lwm.app.server.ClientsStateListener;
import com.lwm.app.server.StreamServer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class ClientsManager {

    private ClientsStateListener listener;

    private final Set<Client> clients;
    private Set<Client> ready = StreamServer.getReadyClients();

    private Context context;

    private Handler handler;

    public ClientsManager(Context context, ClientsStateListener listener){
        this.context = context;
        this.listener = listener;
        clients = StreamServer.getClients();
    }

    public void changeSong() {
        ready.clear();

        handler = new Handler(Looper.getMainLooper());
        handler.post(waitingClients);

        for(final Client client : clients) {
            client.prepare(context).setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) { // Error occurred
                        clients.remove(client);
                    } else { // Everything is OK
                        ready.add(client);
                    }

                    if (clients.equals(ready)) {
                        startClientsPlayback();
                        handler.post(clientsReady);
                    }

                }
            });
        }
    }

    private void startClientsPlayback() {
        for(final Client client : clients) {
            client.start(context).setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) { // Error occurred
                        clients.remove(client);
                    }
                }
            });
        }
    }

    private Runnable waitingClients = new Runnable() {
        @Override
        public void run() {
            if (listener != null) listener.onWaitClients();
        }
    };

    private Runnable clientsReady = new Runnable() {
        @Override
        public void run() {
            if (listener != null) listener.onClientsReady();
        }
    };

    public void pause(){
        Log.d(App.TAG, "ClientsManager: pause");
        for(Client client:clients) {
            client.pause(context);
        }
    }

    public void unpause(int pos){
        Log.d(App.TAG, "ClientsManager: unpause");
        for(Client client:clients) {
            client.unpause(context, pos);
        }
    }

    public void seekTo(int pos) {
        for(Client client:clients) {
            client.seekTo(context, pos);
        }
    }

    public long getClientsMaxPing(){
        return Collections.max(clients, new Comparator<Client>() {
            @Override
            public int compare(Client client, Client client2) {
                return (int) (client.getPing()-client2.getPing());
            }
        }).getPing();
    }

}