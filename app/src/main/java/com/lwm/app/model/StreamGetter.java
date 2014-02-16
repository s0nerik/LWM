/*
package com.lwm.app.model;

import android.content.Context;
import android.util.Log;

import com.lwm.app.App;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamGetter {
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetStream = new HttpGet(App.SERVER_ADDRESS+App.STREAM);
    HttpGet httpGetPosition = new HttpGet(App.SERVER_ADDRESS+App.CURRENT_POSITION);
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    HttpResponse response;

    Context context;

    File dir = context.getCacheDir();
    String source = dir.getAbsolutePath()+"/convertedFile.dat";

    String artistName;
    String songName;

    FileInputStream fis;

    public StreamGetter(Context context){
        this.context = context;
    }

    public String loadStream(){
        try {
            response = httpclient.execute(httpGetStream);
            InputStream content = response.getEntity().getContent();

            File convertedFile = File.createTempFile("convertedFile", ".dat", dir);
            Log.d(App.TAG, "Success: file created.");

            FileOutputStream out = new FileOutputStream(convertedFile);
            Log.d(App.TAG, "Success: out set as output stream.");

            //RIGHT AROUND HERE -----------

            byte buffer[] = new byte[16384];
            int length;
            while ( (length = content.read(buffer)) != -1 )
            {
                out.write(buffer,0, length);
            }

            Log.d(App.TAG, "Success: buffer is filled.");
            out.close();

            return source;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    protected void onPostExecute(Void aVoid) {
//        try {
//            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//            mmr.setDataSource(fis.getFD());
//
//            currentSong.setSource(dir.getAbsolutePath());
//
//            artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//            currentSong.setArtist(artistName);
//
//            songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            currentSong.setName(songName);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
*/
