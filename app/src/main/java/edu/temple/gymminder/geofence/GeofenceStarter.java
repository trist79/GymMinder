package edu.temple.gymminder.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofenceStarter extends BroadcastReceiver {
    public GeofenceStarter() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GeofenceStarter", "Received boot complete");
        Intent serviceIntent = new Intent(context, GeofenceStarterService.class);
        context.startService(serviceIntent);
    }
}
