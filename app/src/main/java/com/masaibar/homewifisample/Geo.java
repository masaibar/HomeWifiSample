package com.masaibar.homewifisample;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by masaibar on 2016/04/06.
 */
public class Geo {

    private static final String TAG = Geo.class.getSimpleName();

    private boolean mIsEnabled;
    private String mLabel;
    private LatLng mLatLng;
    private float mRadius;

    public Geo(boolean isEnabled, String label, LatLng latLng, float radius) {
        mIsEnabled = isEnabled;
        mLabel = label;
        mLatLng = latLng;
        mRadius = radius;
    }

    public boolean isEnabled() {
        return mIsEnabled;
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

}
