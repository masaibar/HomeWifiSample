package com.masaibar.homewifisample.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by masaibar on 2016/03/25.
 */
public class TrackerUtil {

    /**
     * スクリーン表示を送信する
     */
    public static void sendScreenView(@NonNull Tracker tracker, String screenName) {
        if (TextUtils.isEmpty(screenName)) {
            return;
        }
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * イベントを送信する
     */
    public static void sendEvent(@NonNull Tracker tracker, String cateroy, String action) {
        if (TextUtils.isEmpty(cateroy) || TextUtils.isEmpty(action)) {
            return;
        }
        tracker.send(new HitBuilders.EventBuilder()
        .setCategory(cateroy)
        .setAction(action)
        .build());
    }
}
