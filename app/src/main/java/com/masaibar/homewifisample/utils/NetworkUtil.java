package com.masaibar.homewifisample.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by masaibar on 2016/03/24.
 */
public class NetworkUtil {

    private static String TYPE_NAME_WIFI = "WIFI";
    private static int MAX_RETRY_WIFI_SETTING = 2;

    /**
     * Wifiが有効かどうか返す
     */
    public static boolean isEnableOrEnablingWifi(Context context) {
        int wifiState = getWifiState(context);
        return wifiState == WifiManager.WIFI_STATE_ENABLED ||
                wifiState == WifiManager.WIFI_STATE_ENABLING;
    }


    /**
     * Wifiが無効かどうか返す
     */
    public static boolean isDisabledOrDisablingWifi(Context context) {
        int wifiState = getWifiState(context);
        return wifiState == WifiManager.WIFI_STATE_DISABLED ||
                wifiState == WifiManager.WIFI_STATE_DISABLING;
    }

    /**
     * Wifiが未接続かつ無効ならばWifiを有効にする
     */
    public static boolean enableWifiIfDisconneted(Context context) {
        if (isWifiConnected(context) || isEnableOrEnablingWifi(context)) {
            return false;
        }
        return enableWifi(context);
    }

    /**
     * Wifiが未接続且つ有効ならばWifiを無効にする
     */
    public static boolean disableWifiIfDisconnected(Context context) {
        if (isWifiConnected(context)) {
            return false;
        }
        return disableWifi(context);
    }

    /**
     * Wifiを有効にする
     */
    private static boolean enableWifi(Context context) {
        return setWifiSetting(context, true, MAX_RETRY_WIFI_SETTING);
    }

    /**
     * Wifiを無効にする
     */
    private static boolean disableWifi(Context context) {
        return setWifiSetting(context, false, MAX_RETRY_WIFI_SETTING);
    }

    /**
     * 失敗する場合がある(?)ので、リトライできるように
     */
    private static boolean setWifiSetting(Context context, boolean enable, int retryCount) {
        try {
            getWifiManager(context).setWifiEnabled(enable);
            return true;
        } catch (SecurityException e) {
            if (retryCount == 1) {
                return false;
            }
            return setWifiSetting(context, enable, retryCount - 1);
        }
    }

    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    private static boolean isWifiConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        boolean isConnectedOrConnecting = info.isConnectedOrConnecting();
        boolean isEqualWifi = info.getTypeName().equals(TYPE_NAME_WIFI);
        DebugUtil.log("isConnectedOrConnecting = " + isConnectedOrConnecting + ", isEqualWifi =" + isEqualWifi);
        return isConnectedOrConnecting && isEqualWifi;
    }

    /**
     * Wifiの状態を返す
     * WIFI_STATE_DISABLING = 0;
     * WIFI_STATE_DISABLED = 1;
     * WIFI_STATE_ENABLING = 2;
     * WIFI_STATE_ENABLED = 3;
     * WIFI_STATE_UNKNOWN = 4;
     */
    private static int getWifiState(Context context) {
        int wifiState = getWifiManager(context).getWifiState();
        DebugUtil.log("WifiState = " + wifiState);
        return wifiState;
    }
}
