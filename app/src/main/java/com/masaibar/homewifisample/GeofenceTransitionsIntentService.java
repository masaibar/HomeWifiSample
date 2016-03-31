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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.masaibar.homewifisample.utils.DebugUtil;
import com.masaibar.homewifisample.utils.LocationUtil;
import com.masaibar.homewifisample.utils.GoogleAnalyticsUtil;
import com.masaibar.homewifisample.utils.NetworkUtil;

/**
 * Created by masaibar on 2016/03/24.
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TR_CAT = "GeofencingEvent";
    private static final String TR_ACT_HAS_ERROR = "HasError";
    private static final String TR_ACT_ENTER = "Enter";
    private static final String TR_ACT_EXIT = "Exit";

    private GoogleApiClient mGoogleApiClient;
    private boolean mInProgress;

    private GeofencingEvent mGeofencingEvent;

    private Tracker mTracker;

    //参考：http://stackoverflow.com/questions/11859403/no-empty-constructor-when-create-a-service
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTracker = GoogleAnalyticsUtil.getTracker(GeofenceTransitionsIntentService.this);
    }

    @Override
    public void onDestroy() {
        disconnectGoogleApiClient();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mGoogleApiClient == null) {
            return;
        }

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation == null) {
            return;
        }

        DebugUtil.log("latitude = " + lastLocation.getLatitude() + " longitude = " + lastLocation.getLongitude());
        if (mGeofencingEvent == null) {
            return;
        }
        switch (mGeofencingEvent.getGeofenceTransition()) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                onEnter(mGeofencingEvent, lastLocation);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                onExit(mGeofencingEvent, lastLocation);
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

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        mInProgress = false;

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            GoogleAnalyticsUtil.sendEvent(mTracker, TR_CAT, TR_ACT_HAS_ERROR);
            DebugUtil.log("event has error " + event.getErrorCode());
            return;
        }

        mGeofencingEvent = event;
        connectGoogleApiClient();
    }

    private void onEnter(GeofencingEvent event, Location lastLocation) {
        GoogleAnalyticsUtil.sendEvent(mTracker, TR_CAT, TR_ACT_ENTER);
        DebugUtil.log("transition enter");
        int distance = (int) LocationUtil.getDistanceMeters(MapActivity.readLatLng(getApplicationContext()), new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "enter " + distance);
        NetworkUtil.enableWifi(getApplicationContext());
    }

    private void onExit(GeofencingEvent event, Location lastLocation) {
        GoogleAnalyticsUtil.sendEvent(mTracker, TR_CAT, TR_ACT_EXIT);
        int distance = (int) LocationUtil.getDistanceMeters(MapActivity.readLatLng(getApplicationContext()), new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
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

    private PendingIntent getNotificationIntent() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
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
            mGoogleApiClient.disconnect();
        }
    }
}
