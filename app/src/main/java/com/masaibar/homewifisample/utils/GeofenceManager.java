package com.masaibar.homewifisample.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.GeofenceTransitionsIntentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by masaibar on 2016/03/28.
 */
//TODO 単一のGeofenceにしか対応していない、複数対応出来るように
public class GeofenceManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public enum ActionType {
        UPDATE,
        REMOVE
    }

    private Context mContext;
    private ActionType mActionType;

    private GoogleApiClient mGoogleApiClient;

    private Geofence mGeofence;
    private String mRequestId;

    public GeofenceManager(Context context, String requestId) {
        mContext = context;
        mRequestId = requestId;
    }

    /**
     * Geofenceの更新リクエストを投げる
     */
    public void update(LatLng latLng, float radius) {
        mGeofence = new Geofence.Builder()
                .setRequestId(mRequestId)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) //無期限
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT) //fenceへの出入りを監視する
                .setLoiteringDelay(300000)
                .build();

        connect(ActionType.UPDATE);
    }

    private void registGeofence() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(mGeofence);
        GeofencingRequest geofencingRequest = builder.build();

        //TODO 後で切り出し
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                geofencingRequest,
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                DebugUtil.log(status.toString());
                Toast.makeText(mContext, "StatusCode = " + status.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        });

        disconnect();
    }

    public void remove() {
        connect(ActionType.REMOVE);
    }

    private void removeGeofence() {
        List<String> fenceIdList = new ArrayList<>();
        fenceIdList.add(mRequestId);

        if (mGoogleApiClient == null) {
            return;
        }

        //TODO pendingIntentバージョン調べる
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, fenceIdList);
    }

    private void connect(ActionType actionType) {
        mActionType = actionType;

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void disconnect() {
        if (mGoogleApiClient == null) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        DebugUtil.log("GeofenceManager onConnected " + mActionType.toString());

        switch (mActionType) {
            case UPDATE:
                registGeofence();
                break;

            case REMOVE:
                removeGeofence();
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

    }

    private boolean ServiceConnected() {//TODO GooglePlayServiceへの接続確認
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        return true;
    }
}
