package com.masaibar.homewifisample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.WifiUtil;

/**
 * Created by masaibar on 2016/03/24.
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleApiClient mGoogleApiClient;
    private boolean mInProgress;
    private LatLng mLatLng;

    private static final LocationRequest LOCATION_REQUEST = LocationRequest.create()
            .setInterval(5000)
            .setFastestInterval(100)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    //参考：http://stackoverflow.com/questions/11859403/no-empty-constructor-when-create-a-service
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionIntentService");
    }

    @Override
    public void onCreate() {
        mInProgress = false;
        connectGoogleApiClient();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        disconnectGoogleApiClient();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            Log.d("!!!", "event has error");
            Toast.makeText(GeofenceTransitionsIntentService.this, "event has error", Toast.LENGTH_SHORT).show();
            return;
        }

        //Geofence内での動きを取得
        int transition = event.getGeofenceTransition();

        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                onEnter(event);
                break;

//            case Geofence.GEOFENCE_TRANSITION_DWELL:
//                Log.d("!!!", "transition dwell");
//                Toast.makeText(GeofenceTransitionsIntentService.this, "transition dwell", Toast.LENGTH_SHORT).show();
//                sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "dwell");
//                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                onExitFence(event);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        mLatLng = new LatLng(latitude, longitude);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, LOCATION_REQUEST, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (!mInProgress) {
            mInProgress = true;
            mGoogleApiClient.connect();
        }
    }

    private void disconnectGoogleApiClient() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void onEnter(GeofencingEvent event) {
        LatLng savedLatLng = MapActivity.readLatLng(getApplicationContext());
        float distance = LocationUtil.getDistance(mLatLng.latitude, mLatLng.longitude, savedLatLng.latitude, savedLatLng.longitude);
        Log.d("!!!", "transition enter " + distance);
        Toast.makeText(GeofenceTransitionsIntentService.this, "transition enter", Toast.LENGTH_SHORT).show();
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), " enter " + distance);
        WifiUtil.enableWifi(getApplicationContext());
    }

    private void onExitFence(GeofencingEvent event) {
        LatLng savedLatLng = MapActivity.readLatLng(getApplicationContext());
        float distance = LocationUtil.getDistance(mLatLng.latitude, mLatLng.longitude, savedLatLng.latitude, savedLatLng.longitude);
        Log.d("!!!", "transition exit " + distance);
        Toast.makeText(GeofenceTransitionsIntentService.this, "transition exit", Toast.LENGTH_SHORT).show();
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), " exit ");
        WifiUtil.disableWifi(getApplicationContext());
    }

    private void sendNotification(String name, String result) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(name)
                .setContentText(name + result);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

    }
}
