package com.masaibar.homewifisample.utils;

import android.text.TextUtils;
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

    /**
     * debugビルド時のみ、condition == falseならRuntimeExceptionを投げる
     */
    public static void assertion(boolean condition, String message) {
        if (condition || !isDebug()) {
            return;
        }

        RuntimeException e;
        if (TextUtils.isEmpty(message)) {
            e = new RuntimeException();
        } else {
            e = new RuntimeException(message);
        }

        throw e;
    }

    public static void assertion(boolean condition) {
        assertion(condition, null);
    }
}
