package com.lwm.app.server.async.tasks;

import android.os.AsyncTask;

import com.lwm.app.player.StreamPlayer;
import com.lwm.app.server.StreamServer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
* Created by sonerik on 7/13/14.
*/
public class GetPositionAndStart extends AsyncTask<Void, Void, Void> {
    private StreamPlayer streamPlayer;
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetPosition = new HttpGet(StreamServer.SERVER_ADDRESS+StreamServer.CURRENT_POSITION);
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    int pos;
    long correctionStart, correctionEnd, correction;

    public GetPositionAndStart(StreamPlayer streamPlayer) {
        this.streamPlayer = streamPlayer;
    }

    @Override
    protected Void doInBackground(Void... aVoid){

        try {
            streamPlayer.reset();
            streamPlayer.setDataSource(StreamPlayer.STREAM_PATH);
            streamPlayer.prepare();

            correctionStart = System.currentTimeMillis();
            pos = Integer.parseInt(httpclient.execute(httpGetPosition, responseHandler));
            correctionEnd = System.currentTimeMillis();
            correction = correctionEnd - correctionStart;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        streamPlayer.seekTo(pos + (int) (correction / 2.));
        streamPlayer.start();
        new SongInfoGetter(streamPlayer).execute();
    }
}
