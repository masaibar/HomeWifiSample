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
        getWifiManager(context).setWifiEnabled(true);
    }

    /**
     * Wifiを無効にする
     */
    public static void disableWifi(Context context) {
        getWifiManager(context).setWifiEnabled(false);
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
