package com.masaibar.homewifisample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

/**
 * Created by masaibar on 2016/04/06.
 */
public class Geo {

    private static final String TAG = Geo.class.getSimpleName();

    private boolean mIsEnabled;
    private String mId;
    private String mLabel;
    private LatLng mLatLng;
    private float mRadius;

    public Geo(boolean isEnabled, String id, String label, LatLng latLng, float radius) {
        mIsEnabled = isEnabled;
        mId = id;
        mLabel = label;
        mLatLng = latLng;
        mRadius = radius;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public String getId() {
        return mId;
    }

    public String getLabel() {
        return mLabel;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public float getRadius() {
        return mRadius;
    }

    public boolean save(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(TAG+getId(), new Gson().toJson(this)).commit();
    }

    public static Geo getGeo(Context context, String id) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new Gson().fromJson(preferences.getString(TAG+id, null), Geo.class);
    }

    public static boolean hasData(Context context, String id) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(TAG+id);
    }
}
