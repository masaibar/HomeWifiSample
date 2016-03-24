package com.masaibar.homewifisample;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by masaibar on 2016/03/24.
 */
public class WiFiUtil {

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
        getWifiManager(context).setWifiEnabled(true);
    }

    /**
     * Wifiを無効にする
     */
    public static void disableWifi(Context context) {
        getWifiManager(context).setWifiEnabled(false);
    }

    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }
}
