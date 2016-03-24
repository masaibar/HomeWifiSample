package com.masaibar.homewifisample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.masaibar.homewifisample.utils.WifiUtil;

/**
 * Created by masaibar on 2016/03/24.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    //参考：http://stackoverflow.com/questions/11859403/no-empty-constructor-when-create-a-service
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionIntentService");
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

    private void onEnter(GeofencingEvent event) {
        Log.d("!!!", "transition enter");
        Toast.makeText(GeofenceTransitionsIntentService.this, "transition enter", Toast.LENGTH_SHORT).show();
        sendNotification(event.getTriggeringGeofences().get(0).getRequestId(), "enter");
        WifiUtil.enableWifi(getApplicationContext());
    }

    private void onExitFence(GeofencingEvent event) {
        Log.d("!!!", "transition exit");
        Toast.makeText(GeofenceTransitionsIntentService.this, "transition exit", Toast.LENGTH_SHORT).show();
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
}
