package com.masaibar.homewifisample.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by masaibar on 2016/03/22.
 */
public class LocationUtil {

    public static boolean isEnabledGPS(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void jumtToGPSSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * リバースジオコーディングを行う
     * http://seesaawiki.jp/w/moonlight_aska/d/%B0%CC%C3%D6%BE%F0%CA%F3%A4%AB%A4%E9%BD%BB%BD%EA%A4%F2%BC%E8%C6%C0%A4%B9%A4%EB
     */
    public static String getAddressFromLatLng(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        StringBuffer stringBuffer = new StringBuffer();
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            for (Address address : addressList) {
                int index = address.getMaxAddressLineIndex();
                for (int i = 0; i <= index; i++) {
                    stringBuffer.append(address.getAddressLine(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * ジオコーディングを行う
     * @param addressStr 住所文字列
     * @return Latlng 緯度経度
     */
    @Nullable
    public static LatLng getLatLngFromAddress(Context context, String addressStr) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(addressStr, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * (x1, y1)と(x2, y2)の二点間の距離を返す
     */
    public static float getDistance(double x1, double y1, double x2, double y2) {
        float[] results = new float[3];
        Location.distanceBetween(x1, y1, x2, y2, results);
        return results[0];
    }
}
