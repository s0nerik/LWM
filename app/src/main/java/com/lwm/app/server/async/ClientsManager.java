package com.lwm.app.server.async;

import android.os.AsyncTask;
import android.os.Build;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new CommandRunner(client).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        CommandRunner.Command.PING, CommandRunner.Command.PREPARE);
            else
                new CommandRunner(client).execute(CommandRunner.Command.PING, CommandRunner.Command.PREPARE);
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                listener.onWaitClients();
            }

            @Override
            protected Void doInBackground(Void... params) {
                int i = 0;
                try {
                    while(!clients.equals(ready) && i++<5) Thread.sleep(1000);
                    if(i == 5){
                        clients.retainAll(ready);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                listener.onClientsReady();
            }
        }.execute();
    }

    public void pause(){
        Log.d(App.TAG, "ClientsManager: pause");
        for(Client client:clients) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new CommandRunner(client).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommandRunner.Command.PAUSE);
            else
                new CommandRunner(client).execute(CommandRunner.Command.PAUSE);
        }
    }

    public void start(){
        Log.d(App.TAG, "ClientsManager: start");
        for(Client client:clients) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new CommandRunner(client).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommandRunner.Command.PLAY);
            else
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