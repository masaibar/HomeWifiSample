package com.masaibar.homewifisample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.masaibar.homewifisample.utils.DebugUtil;
import com.masaibar.homewifisample.utils.TrackerUtil;
import com.masaibar.homewifisample.utils.WifiUtil;

/**
 * Created by masaibar on 2016/03/24.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TR_CAT = "GeofencingEvent";
    private static final String TR_ACT_HAS_ERROR = "HasError";
    private static final String TR_ACT_ENTER = "Enter";
    private static final String TR_ACT_EXIT = "Exit";

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
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            TrackerUtil.sendEvent(mTracker, TR_CAT, TR_ACT_HAS_ERROR);
            DebugUtil.log("event has error " + event.getErrorCode());
            return;
        }

        switch (event.getGeofenceTransition()) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                onEnter(event);
                break;

//            case Geofence.GEOFENCE_TRANSITION_DWELL:
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

    private void onEnter(GeofencingEvent event) {
        TrackerUtil.sendEvent(mTracker, TR_CAT, TR_ACT_ENTER);
        DebugUtil.log("transition enter");
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "enter");
        WifiUtil.enableWifi(getApplicationContext());
    }

    private void onExitFence(GeofencingEvent event) {
        TrackerUtil.sendEvent(mTracker, TR_CAT, TR_ACT_EXIT);
        DebugUtil.log("transition exit");
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "exit");
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

    private Tracker getTracker() {
        HomeWifiApplication application = (HomeWifiApplication) this.getApplication();
        return application.getDefaultTracker();
    }
}
