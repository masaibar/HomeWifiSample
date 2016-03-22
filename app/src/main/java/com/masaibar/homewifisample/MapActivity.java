package com.masaibar.homewifisample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String PREF_KEY_LAT = "latitude";
    private static final String PREF_KEY_LNG = "longitude";

    private GoogleMap mGoogleMap;
    private Marker mMarker;

    public static void start(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addMarkerSavedLatLngIfNeed();
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMarker != null) {
                    mMarker.remove();
                }

                Toast.makeText(MapActivity.this, latLng.toString(), Toast.LENGTH_SHORT).show();

                mMarker = mGoogleMap.addMarker(getMarkerOptions(latLng));
                saveLatLng(getApplicationContext(), latLng);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
    }

    private MarkerOptions getMarkerOptions(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.snippet(latLng.toString());

        return options;
    }

    private void addMarkerSavedLatLngIfNeed() {
        Context context = getApplicationContext();
        if (hasSavedLatLng(context)) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(context), 15));
            mMarker = mGoogleMap.addMarker(getMarkerOptions(getLatLng(context)));
        }
    }

    public static void saveLatLng(Context context, LatLng latLng) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(PREF_KEY_LAT, Double.doubleToLongBits(latLng.latitude)).commit();
        preferences.edit().putLong(PREF_KEY_LNG, Double.doubleToLongBits(latLng.longitude)).commit();
    }

    public static LatLng getLatLng(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        double latitude = Double.longBitsToDouble(preferences.getLong(PREF_KEY_LAT, 0));
        double longitude = Double.longBitsToDouble(preferences.getLong(PREF_KEY_LNG, 0));
        return new LatLng(latitude, longitude);
    }

    public static boolean hasSavedLatLng(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(PREF_KEY_LAT) && preferences.contains(PREF_KEY_LNG);
    }
}
