package edu.temple.gymminder.geofence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

public class GeofenceStarterService extends Service implements ResultCallback<Status>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "GeofenceStarterService";
    public static final String GEOFENCE_KEY = "Into my ragged coat sleeves";
    public static final String LATITUDE_EXTRA = "Anita: I need ya";
    public static final String LONGITUDE_EXTRA = "Y Y Y Y Y Y Y Y";
    public static final String RADIUS_EXTRA = "I will throw you in the garbage";
    private static final float INVALID_DATA = -999;
    private static final String FILE_NAME = "UnU.owo";

    private GoogleApiClient mGoogleApiClient;
    private double longitude = INVALID_DATA;
    private double latitude = INVALID_DATA;
    private float radius = INVALID_DATA;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras()!=null){
            latitude = intent.getExtras().getDouble(LATITUDE_EXTRA, INVALID_DATA);
            longitude = intent.getExtras().getDouble(LONGITUDE_EXTRA, INVALID_DATA);
            radius = intent.getExtras().getFloat(RADIUS_EXTRA, INVALID_DATA);
            writeGeofenceToFile();
        } else {
            readGeofenceFromFile();
        }
        if(latitude!=INVALID_DATA && longitude!=INVALID_DATA && radius!=INVALID_DATA) {
            connectToPlayServices();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void readGeofenceFromFile() {
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(FILE_NAME));
            latitude = Double.parseDouble(in.readLine());
            longitude = Double.parseDouble(in.readLine());
            radius = Float.parseFloat(in.readLine());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void writeGeofenceToFile() {
        FileOutputStream out;
        String contents = latitude+"\n"+longitude+"\n"+radius;
        try {
            out = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            out.write(contents.getBytes());
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    private void connectToPlayServices(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private void startGeofencing(double latitude, double longitude, float radius) {
        //Remove any old geofences
        ArrayList<String> geoFenceKeys = new ArrayList<>();
        geoFenceKeys.add(GEOFENCE_KEY);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                geoFenceKeys).setResultCallback(this);
        //Create new geofence
        ArrayList<Geofence> fences = new ArrayList<>();
        fences.add(new Geofence.Builder()
                .setRequestId(GEOFENCE_KEY)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER) //GEOFENCE_TRANSITION_DWELL
//                .setLoiteringDelay(6000)
                .build());
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(fences);//TODO make part of intitial building
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, builder.build(),
                getGeofencingPendingIntent()).setResultCallback(this);
    }

    private PendingIntent getGeofencingPendingIntent() {
        Intent intent = new Intent(this, GeofenceIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()) {
            Log.d(TAG, "Geofence created");
            Toast.makeText(this, "Geofence set", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Geofence failed");
        }
        stopSelf();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to Google Play Services");
        startGeofencing(latitude, longitude, radius);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
