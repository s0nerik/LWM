//package com.lwm.app.server.async.tasks;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.lwm.app.App;
//import com.lwm.app.server.StreamServer;
//
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//
//import java.io.IOException;
//
///**
//* Created by sonerik on 7/13/14.
//*/
//public class ClientUnregister extends AsyncTask<Void, Void, Void> {
//    HttpClient httpclient = new DefaultHttpClient();
//    HttpPost httpPostUnregister = new HttpPost(StreamServer.Url.CLIENT_UNREGISTER);
//
//    @Override
//    protected Void doInBackground(Void... aVoid){
//
//        try {
//            httpclient.execute(httpPostUnregister);
//        } catch (IOException e) {
//            Log.e(App.TAG, "Error: ClientUnregister");
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//}
