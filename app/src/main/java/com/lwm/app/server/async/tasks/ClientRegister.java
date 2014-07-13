package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
* Created by sonerik on 7/13/14.
*/
public class ClientRegister extends AsyncTask<Void, Void, Void> {
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPostRegister = new HttpPost(StreamServer.SERVER_ADDRESS+StreamServer.CLIENT_REGISTER);

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            httpclient.execute(httpPostRegister);
        } catch (IOException e) {
            Log.e(App.TAG, "Error: ClientRegister");
            e.printStackTrace();
        }

        return null;
    }

}
