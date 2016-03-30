package com.masaibar.homewifisample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.GeofenceManager;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.TrackerUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static float FENCE_RADIUS_METERS = 50.0f; //Geofence半径 50メートル

    //ジオフェンスID
    private final static String FENCE_ID = "test";

    private GeofenceManager mGeofenceManager;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        mTracker = getTracker();
        mGeofenceManager = new GeofenceManager(context, FENCE_ID);

        findViewById(R.id.button_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackerUtil.sendEvent(mTracker, "Click", "Map");
                MapActivity.start(context);
            }
        });

        findViewById(R.id.button_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = MapActivity.readLatLng(context);
                Toast.makeText(context, latLng.toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(context, LocationUtil.getAddressFromLatLng(context, latLng), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGeofenceManager.update(MapActivity.readLatLng(context), FENCE_RADIUS_METERS);
            }
        });

        findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGeofenceManager.remove();
            }
        });
    }

    @Override
    protected void onResume() {
        TrackerUtil.sendScreenView(mTracker, MainActivity.class.getSimpleName());
        super.onResume();
    }

    private Tracker getTracker() {
        HomeWifiApplication application = (HomeWifiApplication) this.getApplication();
        return application.getDefaultTracker();
    }
}