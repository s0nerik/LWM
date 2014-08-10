package com.lwm.app.server.async;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.model.Client;
import com.lwm.app.server.ClientsStateListener;
import com.lwm.app.server.StreamServer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class ClientsManager {

    ClientsStateListener listener;

    private Set<Client> clients = StreamServer.getClients();
    private Set<Client> ready = StreamServer.getReadyClients();

    private Handler handler;

    public ClientsManager(ClientsStateListener listener){
        this.listener = listener;
    }

    public void changeSong() {
        ready.clear();
        for(Client client:clients) {
            new CommandRunner(client).executeWithThreadPoolExecutor(StreamServer.Method.PING, StreamServer.Method.PREPARE);
        }

        handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                handler.post(waitingClients);
                while (!clients.equals(ready) && i++ < 15) try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                clients.retainAll(ready);
                handler.post(clientsReady);
            }
        }).start();
    }

    private Runnable waitingClients = new Runnable() {
        @Override
        public void run() {
            listener.onWaitClients();
        }
    };

    private Runnable clientsReady = new Runnable() {
        @Override
        public void run() {
            listener.onClientsReady();
        }
    };

    public void pause(){
        Log.d(App.TAG, "ClientsManager: pause");
        for(Client client:clients) {
            new CommandRunner(client).executeWithThreadPoolExecutor(StreamServer.Method.PAUSE);
        }
    }

    public void start(){
        Log.d(App.TAG, "ClientsManager: start");
        for(Client client:clients) {
            new CommandRunner(client).executeWithThreadPoolExecutor(StreamServer.Method.PLAY);
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