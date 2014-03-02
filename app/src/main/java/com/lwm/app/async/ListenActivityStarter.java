package com.lwm.app.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.lwm.app.App;
import com.lwm.app.activity.ListenActivity;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

public class ListenActivityStarter extends AsyncTask<Void, Void, Void> {

    private String artist;
    private String title;
    private String album;
    private String duration;

    Context context;

    String tag;

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetInfo = new HttpGet(App.SERVER_ADDRESS+App.CURRENT_INFO);
    ResponseHandler<String> responseHandler = new BasicResponseHandler();

    ProgressDialog progressDialog;

    public ListenActivityStarter(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, "",
                "Loading song info. Please wait...", true);
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            xpp.setInput(new StringReader(httpclient.execute(httpGetInfo, responseHandler)));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if("song".equals(xpp.getName()) && eventType == XmlPullParser.START_TAG){
                    // Current tag is <song>, so start parsing song info
                    xpp.next();
                    tag = xpp.getName();
                    while(!"song".equals(tag)){
                        // Parse song info while </song> not met
                        if(tag != null){
                            // Tag encountered
                            switch (tag){
                                case "title":
                                    title = xpp.nextText();
                                    break;

                                case "artist":
                                    artist = xpp.nextText();
                                    break;

                                case "album":
                                    album = xpp.nextText();
                                    break;

                                case "duration":
                                    duration = xpp.nextText();
                                    break;
                            }
                        }
                        xpp.next();
                        tag = xpp.getName();
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
        Intent intent = new Intent(context, ListenActivity.class);
        intent.putExtra("artist", artist);
        intent.putExtra("album", album);
        intent.putExtra("title", title);
        intent.putExtra("duration", duration);
        context.startActivity(intent);
    }
}