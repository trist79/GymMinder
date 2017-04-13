package edu.temple.gymminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by rober_000 on 4/12/2017.
 */
public class GeofenceIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceIntentService(String name) {
        super(name);
    }

    public GeofenceIntentService(){
        super("She's laughing like a choir girl");
    }

    private static final String TAG = "GeofenceIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Geofence Service", "Intent:" + intent.getAction());
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event.hasError()){
            Log.e(TAG, "Error: " + event.getErrorCode());
            return;
        }
        Intent startIntent = new Intent(this, MainActivity.class);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("")
                .setSmallIcon(R.drawable.common_full_open_on_phone)
                .setContentText("")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .build();
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
