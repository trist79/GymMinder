package edu.temple.gymminder.geofence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;

import edu.temple.gymminder.MainActivity;
import edu.temple.gymminder.R;

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

    private static final String TAG = "GeofenceIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        Log.d(TAG, "Event:" + !event.hasError());
        if(event.hasError()){
            Log.e(TAG, "Error: " + event.getErrorCode());
            return;
        }
        //Build Intent to start Activity and AdHocFragment
        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.putExtra(MainActivity.START_FRAGMENT_EXTRA, MainActivity.AD_HOC);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_fitness_center_black_24dp)
                .setContentText(getResources().getString(R.string.notification_content))
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .build();
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
