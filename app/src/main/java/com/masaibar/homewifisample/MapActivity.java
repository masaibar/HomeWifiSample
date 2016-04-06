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
import com.google.gson.Gson;
import com.masaibar.homewifisample.utils.LocationUtil;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private Marker mMarker;
    private Circle mCircle;
    private float mZoomLevel = 17.0f; //マップのズームレベル

    //TODO Map開くときIdも渡す必要あり
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
            mZoomLevel = mGoogleMap.getCameraPosition().zoom;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LocationUtil.getLatLngFromAddress(context, addressStr),
                    mZoomLevel)
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setUpMap();
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MapActivity.this, latLng.toString(), Toast.LENGTH_SHORT).show(); //todo 消す
                determinePoint(latLng);
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
        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //do nothing
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                removeCircle();
                addCircle(marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                determinePoint(latLng);
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
        options.draggable(true);
        options.title(LocationUtil.getAddressFromLatLng(getApplicationContext(), latLng));
        options.snippet(latLng.toString());

        return options;
    }

    private CircleOptions getCircleOptions(LatLng latLng) {
        CircleOptions options = new CircleOptions()
                .center(latLng)
                .strokeColor(Color.argb(0x99, 0x33, 0x99, 0xFF))
                .strokeWidth(10.0f)
                .radius(MainActivity.FENCE_RADIUS_METERS); //単位は1.0f/mっぽい
        return options;
    }

    private void setUpMap() {
        Context context = getApplicationContext();
        if(Geo.hasData(context, MainActivity.FENCE_ID)) {
            setUpPoint(readLatLng(context));
        } else {
            //TODO 保存してある座標がない場合は現在地を表示
        }
    }

    private void setUpPoint(LatLng latLng) {
        determinePoint(latLng, true);
    }

    private void determinePoint(LatLng latLng) {
        determinePoint(latLng, false);
    }

    private void determinePoint(LatLng latLng, boolean isSetup) {
        removeMarker();
        removeCircle();
        if (mGoogleMap == null) {
            return;
        }
        if (!isSetup) {
            mZoomLevel = mGoogleMap.getCameraPosition().zoom;
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel));
        addMarker(latLng);
        addCircle(latLng);
        saveLatLng(getApplicationContext(), latLng);
    }

    public static void saveLatLng(Context context, LatLng latLng) {
        new Geo(true, MainActivity.FENCE_ID, "home", latLng, MainActivity.FENCE_RADIUS_METERS).save(context);
    }

    public static LatLng readLatLng(Context context) {
        Geo geo = Geo.getGeo(context, MainActivity.FENCE_ID);
        LatLng latLng = geo.getLatLng();
        return new LatLng(latLng.latitude, latLng.longitude);
    }

    private void addMarker(LatLng latLng) {
        mMarker = mGoogleMap.addMarker(getMarkerOptions(latLng));
    }

    private void removeMarker() {
        if (mMarker != null) {
            mMarker.remove();
        }
    }

    private void addCircle(LatLng latLng) {
        mCircle = mGoogleMap.addCircle(getCircleOptions(latLng));
    }

    private void removeCircle() {
        if (mCircle != null) {
            mCircle.remove();
        }
    }
}
