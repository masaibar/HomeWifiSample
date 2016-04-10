package com.masaibar.homewifisample;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by masaibar on 2016/04/10.
 */
public class GeoHashMap extends HashMap<String, Geo> {

    public boolean hasKey(String fenceId) {
        return this.size() > 0 && this.containsKey(fenceId);
    }

    public String getLabel(String fenceId) {
        return this.get(fenceId).getLabel();
    }

    public LatLng getLatLng(String fenceId) {
        return this.get(fenceId).getLatLng();
    }

    public float getRadius(String fenceId) {
        return this.get(fenceId).getRadius();
    }
}
