package com.masaibar.homewifisample;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.deploygate.sdk.DeployGate;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;

/**
 * Created by masaibar on 2016/03/25.
 * https://developers.google.com/analytics/devguides/collection/android/v4/#-
 */

public class App extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(getApplicationContext(), new Crashlytics());
        DeployGate.install(this);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(getApplicationContext());
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
