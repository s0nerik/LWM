package com.lwm.app.model;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.lwm.app.App;
import com.lwm.app.server.StreamServer;
import com.squareup.okhttp.OkHttpClient;

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

    public Future<String> prepare(Context context) {
        return executeMethodRequest(context, StreamServer.Method.PREPARE);
    }

    public Future<String> start(Context context) {
        return executeMethodRequest(context, StreamServer.Method.START);
    }

    public Future<String> pause(Context context) {
        return executeMethodRequest(context, StreamServer.Method.PAUSE);
    }

    public Future<String> unpause(Context context) {
        return executeMethodRequest(context, StreamServer.Method.UNPAUSE);
    }

    public Future<String> startFrom(Context context, int pos) {
        return Ion.with(context)
                .load(getClientAddress() + StreamServer.Method.START_FROM)
                .noCache()
                .setLogging(App.TAG, Log.DEBUG)
                .setBodyParameter(StreamServer.Params.POSITION, String.valueOf(pos + ping))
                .asString();
    }

    public Future<String> seekTo(Context context, int pos) {
        return Ion.with(context)
                .load(getClientAddress() + StreamServer.Method.SEEK_TO)
                .noCache()
                .setLogging(App.TAG, Log.DEBUG)
                .setBodyParameter(StreamServer.Params.POSITION, String.valueOf(pos + ping))
                .asString();
    }

}