package com.lwm.app.model;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.lwm.app.App;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;

import org.apache.http.message.BasicNameValuePair;

import java.util.Arrays;
import java.util.List;

public class Client {

    private long ping = -1;
    private String ip;

    public Client(String ip) {
        this.ip = ip;
    }

    public String getIP() {
        return ip;
    }

    public long getPing() {
        return ping;
    }

    public String getClientAddress() {
        return "http://" + ip + ":" + StreamServer.PORT;
    }

    private OkHttpClient httpClient = new OkHttpClient();

    public void setPing(long ping) {
        if (this.ping != -1) {
            this.ping = Math.round((this.ping + ping * 2) / 3f); // New ping priority is higher
        } else {
            this.ping = ping;
        }
    }

    @Override
    public boolean equals(Object o) {
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        int result = 17;
        return 37 * result + (ip == null ? 0 : ip.hashCode());
    }

    private Future<String> executeMethodRequest(Context context, String method) {
        return Ion.with(context)
                .load(getClientAddress() + method)
                .noCache()
                .setLogging(App.TAG, Log.DEBUG)
                .setStringBody("")
                .asString();
    }

    private Future<String> executeMethodRequest(Context context, String method, List<BasicNameValuePair> params) {
        Builders.Any.B builder = Ion.with(context)
                .load(getClientAddress() + method)
                .noCache()
                .setLogging(App.TAG, Log.DEBUG);

        for (BasicNameValuePair param : params) {
            builder.setBodyParameter(param.getName(), param.getValue());
        }

        return builder.asString();
    }

    public Future<String> prepare(Context context) {
        return executeMethodRequest(context, StreamServer.Method.PREPARE);
    }

    public Future<String> start(Context context) {
        return executeMethodRequest(context, StreamServer.Method.START);
    }

    public Future<String> pause(Context context) {
        return executeMethodRequest(context, StreamServer.Method.PAUSE);
    }

    public Future<String> unpause(Context context, int pos) {
        return executeMethodRequest(context, StreamServer.Method.UNPAUSE,
                Arrays.asList(new BasicNameValuePair(StreamServer.Params.POSITION, String.valueOf(pos + ping))));
    }

    public Future<String> startFrom(Context context, int pos) {
        return executeMethodRequest(context, StreamServer.Method.START_FROM,
                Arrays.asList(new BasicNameValuePair(StreamServer.Params.POSITION, String.valueOf(pos + ping))));
    }

    public Future<String> seekTo(Context context, int pos) {
        return executeMethodRequest(context, StreamServer.Method.SEEK_TO,
                Arrays.asList(new BasicNameValuePair(StreamServer.Params.POSITION, String.valueOf(pos + ping))));
    }

}