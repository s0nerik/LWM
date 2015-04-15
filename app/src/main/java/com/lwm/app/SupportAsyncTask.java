package com.lwm.app;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by sonerik on 8/10/14.
 */
public abstract class SupportAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public SupportAsyncTask<Params, Progress, Result> executeWithThreadPoolExecutor(Params... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            this.execute(params);
        return this;
    }

}
