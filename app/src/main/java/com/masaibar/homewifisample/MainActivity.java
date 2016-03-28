package com.masaibar.homewifisample;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.TrackerUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {

    //半径
    public final static float FENCE_RADIUS_METERS = 50.0f; //50メートル

    //ジオフェンスID
    private final static String FENCE_ID = "test";

    private enum RequestType {
        ADD_FENCE,   //設置
        REMOVE_FENCE //削除
    }

    private boolean mInProgress;
    private RequestType mRequestType;
    private GoogleApiClient mGoogleApiClient;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTracker = getTracker();

        final Context context = getApplicationContext();
        mInProgress = false;

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
                mRequestType = RequestType.ADD_FENCE;
                connectGoogleApiClient();
            }
        });

        findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestType = RequestType.REMOVE_FENCE;
                connectGoogleApiClient();
            }
        });
    }

    @Override
    protected void onResume() {
        TrackerUtil.sendScreenView(mTracker, MainActivity.class.getSimpleName());
        super.onResume();
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();
        if (!mInProgress) {
            mInProgress = true;
            mGoogleApiClient.connect();
        }
    }

    private void disconnectGoogleApiClient() {
        mInProgress = false;
        if (mGoogleApiClient == null) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LatLng latLng = MapActivity.readLatLng(getApplicationContext());
        switch (mRequestType) {
            case ADD_FENCE:
                addFence(
                        latLng.latitude,
                        latLng.longitude,
                        FENCE_RADIUS_METERS,
                        FENCE_ID
                );
                break;

            case REMOVE_FENCE:
                removeFence(FENCE_ID);
                break;

            default:
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
    }

    private boolean ServiceConnected() {//TODO GooglePlayServiceへの接続確認
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        return true;
    }

    private void addFence(double latitude, double longitude, float radius, String requestId) {
        Toast.makeText(MainActivity.this, "addFence", Toast.LENGTH_SHORT).show();

        Geofence geofence = new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) //無期限
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT) //fenceへの出入りを監視する
                .build();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        GeofencingRequest geofencingRequest = builder.build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                geofencingRequest,
                getGeofencePendingIntent()
        ).setResultCallback(this);

        disconnectGoogleApiClient();
    }

    private PendingIntent getGeofencePendingIntent() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void removeFence(String requestId) {
        Toast.makeText(MainActivity.this, "removeFence", Toast.LENGTH_SHORT).show();
        List<String> fenceIdList = new ArrayList<>();
        fenceIdList.add(requestId);

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, fenceIdList);

        disconnectGoogleApiClient();
    }

    @Override
    public void onResult(Result result) {

    }

    private Tracker getTracker() {
        HomeWifiApplication application = (HomeWifiApplication) this.getApplication();
        return application.getDefaultTracker();
    }
}
