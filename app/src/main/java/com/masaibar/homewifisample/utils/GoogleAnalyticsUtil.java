package com.masaibar.homewifisample.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.masaibar.homewifisample.App;

/**
 * Created by masaibar on 2016/03/25.
 */
public class GoogleAnalyticsUtil {

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

    /**
     * カスタムされた例外の送信
     */
    private static void sendCustomException(@NonNull Tracker tracker, String desc, boolean isFatal) {
        if (TextUtils.isEmpty(desc)) {
            desc = "";
        }
        tracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(desc)
                .setFatal(isFatal)
                .build());
    }

    /**
     * 標準的な例外の送信
     */
    private static void sendStandardException(Context context, Tracker tracker, Exception e) {
        tracker.send(new HitBuilders.ExceptionBuilder()
        .setDescription(new StandardExceptionParser(context, null).getDescription(Thread.currentThread().getName(), e))
        .setFatal(false)
        .build());
    }

    public static Tracker getTracker(Activity activity) {
        return ((App) activity.getApplication()).getDefaultTracker();
    }

    public static Tracker getTracker(Service service) {
        return ((App) service.getApplication()).getDefaultTracker();
    }
}
