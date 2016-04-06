package com.masaibar.homewifisample;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.GeofenceManager;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.NetworkUtil;
import com.masaibar.homewifisample.utils.GoogleAnalyticsUtil;

public class MainActivity extends AppCompatActivity {

    public final static float FENCE_RADIUS_METERS = 50.0f; //Geofence半径 50メートル

    //ジオフェンスID
    public final static String FENCE_ID = "test";

    private GeofenceManager mGeofenceManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        mTracker = GoogleAnalyticsUtil.getTracker(MainActivity.this);
        mGeofenceManager = new GeofenceManager(context, FENCE_ID);

        findViewById(R.id.button_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleAnalyticsUtil.sendEvent(mTracker, "Click", "Map");
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

        final Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng targetLatLng = MapActivity.readLatLng(context);
                mGeofenceManager.update(targetLatLng, FENCE_RADIUS_METERS);
                if (mLastLocation == null) {
                    return;
                }
                float distance = LocationUtil.getDistanceMeters(
                        new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        targetLatLng
                );
                if (distance > FENCE_RADIUS_METERS) {
                    //対象範囲外から設定した際の処理
                    NetworkUtil.disableWifiIfDisconnected(context);
                }
            }
        });

        findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGeofenceManager.remove();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        buttonStart.setEnabled(true);
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        buttonStart.setEnabled(false);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        buttonStart.setEnabled(false);
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView(mTracker, MainActivity.class.getSimpleName());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectGoogleApiClient();
    }

    private void disconnectGoogleApiClient() {
        if (mGoogleApiClient == null) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}