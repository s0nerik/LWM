package com.lwm.app.server.async;

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

    Set<Client> clients = StreamServer.getClients();
    Set<Client> ready = StreamServer.getReadyClients();

    public ClientsManager(ClientsStateListener listener){
        this.listener = listener;
    }

    public void changeSong() {
        ready.clear();
        for(Client client:clients) {
            new CommandRunner(client).execute(CommandRunner.Command.PING, CommandRunner.Command.PREPARE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                try {
                    listener.onWaitClients();
                    while(!clients.equals(ready) && i++<10) Thread.sleep(1000);
                    listener.onClientsReady();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void pause(){
        Log.d(App.TAG, "ClientsManager: pause");
        for(Client client:clients) {
            new CommandRunner(client).execute(CommandRunner.Command.PAUSE);
        }
    }

    public void start(){
        Log.d(App.TAG, "ClientsManager: start");
        for(Client client:clients) {
            new CommandRunner(client).execute(CommandRunner.Command.PLAY);
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