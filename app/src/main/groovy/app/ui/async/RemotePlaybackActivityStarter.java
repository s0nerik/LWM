package app.ui.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import app.server.StreamServer;
import app.ui.activity.RemotePlaybackActivity;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RemotePlaybackActivityStarter extends AsyncTask<Void, Void, Void> {

    private String artist;
    private String title;
    private String album;
    private String durationString;
    private int duration;

    Context context;

    String tag;

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGetInfo = new HttpGet(StreamServer.Url.CURRENT_INFO);
    ResponseHandler<String> responseHandler = new BasicResponseHandler();

    ProgressDialog progressDialog;

    public RemotePlaybackActivityStarter(Context context){
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
            JSONObject jsonObject = new JSONObject(httpclient.execute(httpGetInfo, responseHandler));
            artist = jsonObject.getString("artist");
            title = jsonObject.getString("title");
            album = jsonObject.getString("album");
            durationString = jsonObject.getString("duration_minutes");
            duration = jsonObject.getInt("duration");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
        Intent intent = new Intent(context, RemotePlaybackActivity.class);
        intent.putExtra("artist", artist);
        intent.putExtra("album", album);
        intent.putExtra("title", title);
        intent.putExtra("duration_string", durationString);
        intent.putExtra("duration", duration);
        context.startActivity(intent);
    }
}