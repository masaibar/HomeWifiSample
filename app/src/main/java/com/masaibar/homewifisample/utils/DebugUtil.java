package com.masaibar.homewifisample.utils;

import android.util.Log;

import com.masaibar.homewifisample.BuildConfig;

/**
 * Created by masaibar on 2016/03/25.
 */
public class DebugUtil {
    private static final String TAG = DebugUtil.class.getSimpleName();

    public static boolean isDebug() {
        return BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug");
    }

    public static boolean isRelease() {
        return BuildConfig.BUILD_TYPE.equalsIgnoreCase("release");
    }

    public static void log(String str) {
        if (!isDebug()) {
            return;
        }
        Log.d(TAG, str);
    }

    public static void log(String format, Object... args) {
        if (!isDebug()) {
            return;
        }

        Log.d(TAG, String.format(format, args));
    }
}
