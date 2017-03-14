package com.axiang.newcoolweather.android.activities;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by a2389 on 2017/3/2.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext() {
        return context;
    }
}
