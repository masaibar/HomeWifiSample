package com.masaibar.homewifisample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.GeofenceManager;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.NetworkUtil;
import com.masaibar.homewifisample.utils.TrackerUtil;

public class MainActivity extends AppCompatActivity {

    public final static float FENCE_RADIUS_METERS = 50.0f; //Geofence半径 50メートル

    //ジオフェンスID
    private final static String FENCE_ID = "test";

    private GeofenceManager mGeofenceManager;
    private GoogleApiClient mGoogleApiClient;

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

        final Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng targetLatLng = MapActivity.readLatLng(context);
                mGeofenceManager.update(targetLatLng, FENCE_RADIUS_METERS);
                float distance = LocationUtil.getDistanceMetersFromCurrentLocation(mGoogleApiClient, targetLatLng);
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
        TrackerUtil.sendScreenView(mTracker, MainActivity.class.getSimpleName());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectGoogleApiClient();
    }

    private Tracker getTracker() {
        HomeWifiApplication application = (HomeWifiApplication) this.getApplication();
        return application.getDefaultTracker();
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