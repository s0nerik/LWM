//package com.lwm.app.model;
//
//import android.os.AsyncTask;
//
//import com.lwm.app.App;
//
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.BasicResponseHandler;
//import org.apache.http.impl.client.DefaultHttpClient;
//
//import java.io.IOException;
//
//public class PositionGetter extends AsyncTask<Void, Void, Void>{
//
//    HttpClient httpclient = new DefaultHttpClient();
//    HttpGet httpGetPosition = new HttpGet(App.SERVER_ADDRESS+App.CURRENT_POSITION);
//    ResponseHandler<String> responseHandler = new BasicResponseHandler();
//    MusicPlayer mp = MusicPlayer.getInstance();
//    String pos;
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//        ResponseHandler<String> responseHandler = new BasicResponseHandler();
//        try {
////            mp.prepare();
//            pos = httpclient.execute(httpGetPosition, responseHandler);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        mp.seekTo(Integer.parseInt(pos));
//        mp.start();
//    }
//}