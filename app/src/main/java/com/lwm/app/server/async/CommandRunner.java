//package com.lwm.app.server.async;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.koushikdutta.async.future.FutureCallback;
//import com.koushikdutta.ion.Ion;
//import com.lwm.app.App;
//import com.lwm.app.SupportAsyncTask;
//import com.lwm.app.model.Client;
//import com.lwm.app.server.StreamServer;
//
//public class CommandRunner extends SupportAsyncTask<String, Void, Void> {
//
//    private static final int MAX_RETRIES = 3;
//
//    private Client client;
//
//    private Context context;
//
//    public CommandRunner(Context context, Client client) {
//        this.client = client;
//        this.context = context;
//    }
//
//    @Override
//    protected Void doInBackground(String... methods) {
//        for(String method:methods){
//            sendRequest(method);
//        }
//        return null;
//    }
//
//    private void sendRequest(String method) {
//        sendRequest(method, 0);
//    }
//
//    private void sendRequest(final String method, int retryCount) {
//        if(StreamServer.Method.PING.equals(method)){
//            final long start = System.currentTimeMillis();
//            System.setProperty("http.keepAlive", "false");
//            Ion.with(context)
//                    .load("http://" + client.getIP() + ":8888" + StreamServer.Method.PING)
//                    .noCache()
//                    .setLogging(App.TAG, Log.DEBUG)
//                    .asString()
//                    .withResponse()
//                    .setCallback(new FutureCallback<com.koushikdutta.ion.Response<String>>() {
//                        @Override
//                        public void onCompleted(Exception e, com.koushikdutta.ion.Response<String> result) {
//                            long ping = System.currentTimeMillis() - start;
//                            client.setPing(Math.round(ping / 2.));
//
//                            // Debug
//                            Log.d(App.TAG, "Ping: " + client.getPing());
//                        }
//                    });
//        } else {
//
//            Ion.with(context)
//                    .load("http://" + client.getIP() + ":8888" + method)
//                    .noCache()
//                    .setLogging(App.TAG, Log.DEBUG)
//                    .asString()
//                    .withResponse()
//                    .setCallback(new FutureCallback<com.koushikdutta.ion.Response<String>>() {
//                        @Override
//                        public void onCompleted(Exception e, com.koushikdutta.ion.Response<String> result) {
//                            if (e == null) Log.d(App.TAG, "Method "+method+" completed.");
//                            else Log.e(App.TAG, "Method "+method+" error.", e);
//                        }
//                    });
//
////            Request request = new Request.Builder()
////                    .url("http://" + client.getIP() + ":8888" + method)
////                    .addHeader("Content-Length", "0")
////                    .post(RequestBody.create(MediaType.parse("text/plain"), ""))
////                    .build();
////
////            try {
////                Response response = httpClient.newCall(request).execute();
////                Log.d(App.TAG, "Response: " + response.body().string());
////
////            } catch (EOFException e) {
////                // TODO: workaround this bug (or use another HttpClient)
////                Log.e(App.TAG, "EOFException in CommandRunner, method: " + method, e);
////                if (retryCount < MAX_RETRIES) {
////                    sendRequest(method, retryCount + 1);
////                }
////            } catch (IOException e) {
////                Log.e(App.TAG, "IOException in CommandRunner, method: " + method);
////                Log.e(App.TAG, "", e);
////                StreamServer.removeClient(client);
////            }
//        }
//    }
//
//}