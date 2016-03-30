package com.masaibar.homewifisample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.DebugUtil;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.TrackerUtil;
import com.masaibar.homewifisample.utils.NetworkUtil;

/**
 * Created by masaibar on 2016/03/24.
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TR_CAT = "GeofencingEvent";
    private static final String TR_ACT_HAS_ERROR = "HasError";
    private static final String TR_ACT_ENTER = "Enter";
    private static final String TR_ACT_DWELL = "Dwell";
    private static final String TR_ACT_EXIT = "Exit";

    private GoogleApiClient mGoogleApiClient;
    private boolean mInProgress;
    private Location mLastLocation;

    private GeofencingEvent mGeofencingEvent;

    private Tracker mTracker;

    //参考：http://stackoverflow.com/questions/11859403/no-empty-constructor-when-create-a-service
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTracker = getTracker();
    }

    @Override
    public void onDestroy() {
        disconnectGoogleApiClient();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mGoogleApiClient != null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                DebugUtil.log("latitude = " + mLastLocation.getLatitude() + " longitude = " + mLastLocation.getLongitude());
                switch (mGeofencingEvent.getGeofenceTransition()) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        onEnter(mGeofencingEvent);
                        break;

                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        onExit(mGeofencingEvent);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        mInProgress = false;
        connectGoogleApiClient();

        //TODO 接続されたうえで後の処理を行えるようにcallback入れたほうが良さそうかも

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            TrackerUtil.sendEvent(mTracker, TR_CAT, TR_ACT_HAS_ERROR);
            DebugUtil.log("event has error " + event.getErrorCode());
            return;
        }

        mGeofencingEvent = event;
    }

    private void onEnter(GeofencingEvent event) {
        TrackerUtil.sendEvent(mTracker, TR_CAT, TR_ACT_ENTER);
        DebugUtil.log("transition enter");
        int distance = (int) LocationUtil.getDistanceMeters(MapActivity.readLatLng(getApplicationContext()), new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "enter " + distance);
        NetworkUtil.enableWifi(getApplicationContext());
    }

    private void onExit(GeofencingEvent event) {
        TrackerUtil.sendEvent(mTracker, TR_CAT, TR_ACT_EXIT);
        int distance = (int) LocationUtil.getDistanceMeters(MapActivity.readLatLng(getApplicationContext()), new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        DebugUtil.log("transition exit");
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "exit " + distance);
        NetworkUtil.disableWifi(getApplicationContext());
    }

    private void sendNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentIntent(getNotificationIntent())
                .setContentText(text);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private Tracker getTracker() {
        HomeWifiApplication application = (HomeWifiApplication) this.getApplication();
        return application.getDefaultTracker();
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(GeofenceTransitionsIntentService.this)
                .addOnConnectionFailedListener(GeofenceTransitionsIntentService.this)
                .build();
        if (!mInProgress) {
            mInProgress = true;
            mGoogleApiClient.connect();
        }
    }

    private void disconnectGoogleApiClient() {
        if (mGoogleApiClient == null) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private PendingIntent getNotificationIntent() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }
}
