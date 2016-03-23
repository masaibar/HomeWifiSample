package com.masaibar.homewifisample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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

        final EditText editAddress = (EditText) findViewById(R.id.edit_address);
        editAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) { //EditTextのフォーカス外れたらソフトウェアキーボードを閉じる
                    InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
        editAddress.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchAndMoveCamera(editAddress);
                }
                return false;
            }
        });
    }

    private void searchAndMoveCamera(EditText editText) {
        Context context = getApplicationContext();
        String addressStr = editText.getText().toString();

        if (!TextUtils.isEmpty(addressStr)) {
            if (mGoogleMap == null) {
                return;
            }
            addMarkerAndAnimateCamera(LocationUtil.getLatLngFromAddress(context, addressStr));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addMarkerAndAnimateCameraSavedLatLngIfNeed();
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
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (!LocationUtil.isEnabledGPS(getApplicationContext())) {
                    //todo あとでダイアログはさもう http://www.noveluck.co.jp/blog/archives/159 http://mslgt.hatenablog.com/entry/2015/12/29/004133
                    LocationUtil.jumtToGPSSettings(getApplicationContext());
                }
                return false;
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
        options.title(LocationUtil.getAddressFromLatLng(getApplicationContext(), latLng));
        options.snippet(latLng.toString());

        return options;
    }

    private CircleOptions getCircleOptions(LatLng latLng) {
        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius(200.0f); //単位は1.0f/mっぽい
        return options;
    }

    private void addMarkerAndAnimateCameraSavedLatLngIfNeed() {
        Context context = getApplicationContext();
        if (hasSavedLatLng(context)) {
            addMarkerAndAnimateCamera(readLatLng(context));
        }
    }

    private void addMarkerAndAnimateCamera(LatLng latLng) {
        if (mMarker != null) {
            mMarker.remove();
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMarker = mGoogleMap.addMarker(getMarkerOptions(latLng));
        Circle circle = mGoogleMap.addCircle(getCircleOptions(latLng));
        circle.setStrokeColor(Color.argb(0x99, 0x33, 0x99, 0xFF));
        circle.setStrokeWidth(10.0f);
    }

    public static void saveLatLng(Context context, LatLng latLng) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(PREF_KEY_LAT, Double.doubleToLongBits(latLng.latitude)).commit();
        preferences.edit().putLong(PREF_KEY_LNG, Double.doubleToLongBits(latLng.longitude)).commit();
    }

    public static LatLng readLatLng(Context context) {
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
