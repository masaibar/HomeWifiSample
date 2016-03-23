package com.masaibar.homewifisample;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback{

    //半径
    private final static float FENCE_RADIUS_METERS = 200.0f;

    //ジオフェンスID
    private final static String FENCE_ID = "test";

    //設置、削除を示す定数
    private final static int ADD_FENCE = 0;
    private final static int REMOVE_FENCE = 1;

    private boolean mInProgress;
    private int mRequestType;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        mInProgress = false;

        findViewById(R.id.button_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapActivity.start(getApplicationContext());
            }
        });

        findViewById(R.id.button_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        MainActivity.this,
                        MapActivity.getLatLng(getApplicationContext()).toString(),
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(
                        MainActivity.this,
                        LocationUtil.getAddressFromLatLng(getApplicationContext(), MapActivity.getLatLng(getApplicationContext())),
                        Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestType = ADD_FENCE;
                connectGoogleApiClient();
            }
        });

        findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestType = REMOVE_FENCE;
                connectGoogleApiClient();
            }
        });


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

    @Override
    public void onConnected(Bundle bundle) {
        LatLng latLng = MapActivity.getLatLng(getApplicationContext());
        switch (mRequestType) {
            case ADD_FENCE:
                addFence(
                        latLng.latitude,
                        latLng.longitude,
                        FENCE_RADIUS_METERS,
                        FENCE_ID,
                        "http://www.yahoo.co.jp"
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

    private boolean ServiceConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        return true;
    }

    private void addFence(double latitude, double longitude, float radius, String requestId, String broadcastUrl) {

        Geofence geofence = new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) //無期限
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER) //fenceに入ったことを認識
                .build();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        GeofencingRequest geofencingRequest = builder.build();

        //フェンス内に入った時に指定のURIを表示するインテントを投げるようにする
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(broadcastUrl));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                geofencingRequest,
                pendingIntent
        ).setResultCallback(this);
    }

    private void removeFence(String requestId) {
        List<String> fenceIdList = new ArrayList<>();
        fenceIdList.add(requestId);

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, fenceIdList);
    }

    @Override
    public void onResult(Result result) {

    }
}
