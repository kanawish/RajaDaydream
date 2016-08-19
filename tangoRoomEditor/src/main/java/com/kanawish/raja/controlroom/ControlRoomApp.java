package com.kanawish.raja.controlroom;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by kanawish on 2016-07-17.
 */

public class ControlRoomApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } // NOTE: No logging in release mode.
    }
}
