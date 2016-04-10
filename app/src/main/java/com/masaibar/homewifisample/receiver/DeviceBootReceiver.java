package com.masaibar.homewifisample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.masaibar.homewifisample.Geo;
import com.masaibar.homewifisample.GeoHashMap;
import com.masaibar.homewifisample.GeoHashMapManager;
import com.masaibar.homewifisample.utils.DebugUtil;
import com.masaibar.homewifisample.utils.GeofenceManager;

import java.util.Map;

/**
 * Created by masaibar on 2016/04/10.
 * 参考:http://qiita.com/amay077/items/e740015c00a6e7d45f6b
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DebugUtil.log("!!! BootCompletedReceiver onReceive, action :" + intent.getAction());

        GeoHashMap geoHashMap = GeoHashMapManager.getInstance().getSavedGeoHashMap(context);

        //保存されているGeoFenceをぜんぶ舐めて、設定が有効なら再設定を行う
        for (Map.Entry<String, Geo> entry : geoHashMap.entrySet()) {
            String fenceId = entry.getKey();
            Geo geo = entry.getValue();
            if (geo.isEnabled()) {
                new GeofenceManager(context).update(fenceId, geo);
            }
        }
    }
}
