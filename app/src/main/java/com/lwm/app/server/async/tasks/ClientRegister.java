package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

/**
* Created by sonerik on 7/13/14.
*/
public class ClientRegister extends AsyncTask<Void, Void, Void> {
    private OkHttpClient httpClient = new OkHttpClient();
    private Request httpPostRegister = new Request.Builder()
            .url(StreamServer.Url.CLIENT_REGISTER)
            .post(RequestBody.create(null, ""))
            .build();

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            httpClient.newCall(httpPostRegister).execute().body().close();
        } catch (IOException e) {
            Log.e(App.TAG, "Error: ClientRegister");
            e.printStackTrace();
        }

        return null;
    }

}
