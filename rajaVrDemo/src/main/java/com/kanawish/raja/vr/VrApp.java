package com.kanawish.raja.vr;

import android.app.Application;
import android.util.Log;

import timber.log.Timber;

/**
 * Created by kanawish on 2016-08-01.
 */

public class VrApp extends Application {
    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } // NOTE: No logging in release mode.
    }

}
