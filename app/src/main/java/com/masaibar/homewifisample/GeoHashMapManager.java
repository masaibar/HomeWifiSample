package com.masaibar.homewifisample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * Created by masaibar on 2016/04/10.
 */
public class GeoHashMapManager {
    private static final String TAG = GeoHashMapManager.class.getSimpleName();
    private static int MAX_HASHMAP_SIZE = 100;

    private static GeoHashMapManager sInstance;

    public static GeoHashMapManager getInstance() {
        if (sInstance == null) {
            sInstance = new GeoHashMapManager();
        }
        return sInstance;
    }

    public int getNextKey(Context context) {
        return getSavedGeoHashMap(context).size();
    }

    public boolean save(Context context, GeoHashMap geoHashMap) {
        String value = new Gson().toJson(geoHashMap);
        return getPrefs(context).edit().putString(TAG, value).commit();
    }


    public GeoHashMap getSavedGeoHashMap(Context context) {
        String value = getPrefs(context).getString(TAG, null);
        return hashSavedData(context) && !TextUtils.isEmpty(value) ?
                new Gson().fromJson(value, GeoHashMap.class) :
                new GeoHashMap();
    }

    public boolean removeAll(Context context) {
        return getPrefs(context).edit().remove(TAG).commit();
    }

    private boolean hashSavedData(Context context) {
        return getPrefs(context).contains(TAG);
    }

    private SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
