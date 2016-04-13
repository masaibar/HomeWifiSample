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
     * Wifiの状態を返す
     */
    public static int getWifiState(Context context) {
        return getWifiManager(context).getWifiState();
    }

    /**
     * Wifiを有効にする
     */
    public static void enableWifi(Context context) {
        if(isWifiConnected(context)) {
            return;
        }
        setWifiSetting(context, true, MAX_RETRY_WIFI_SETTING);
    }

    /**
     * Wifiを無効にする
     */
    public static void disableWifi(Context context) {
        setWifiSetting(context, false, MAX_RETRY_WIFI_SETTING);
    }

    /**
     * Wifiが未接続且つ有効ならばWifiを無効にする
     */
    public static void disableWifiIfDisconnected(Context context) {
        if (isWifiConnected(context)) {
            return;
        }
        disableWifi(context);
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
            return setWifiSetting(context, enable, retryCount -1);
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
        return info.isConnectedOrConnecting() && info.getTypeName().equals(TYPE_NAME_WIFI);
    }
}
