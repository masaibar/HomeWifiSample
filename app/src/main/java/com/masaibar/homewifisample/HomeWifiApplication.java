package com.masaibar.homewifisample;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by masaibar on 2016/03/25.
 * https://developers.google.com/analytics/devguides/collection/android/v4/#-
 */

public class HomeWifiApplication extends Application {
    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(getApplicationContext());
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
