package edu.temple.gymminder;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GeofenceFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

    private int hmmm = 0;
    public GeofenceFragment() {
        // Required empty public constructor
    }

    private PendingIntent geoFencingPendingIntent;
    private static final long LOCATION_UPDATE_INTERVAL = BuildConfig.DEBUG ? 5000 : 60000;
    private static final int DEFAULT_RADIUS = 100;
    private static final String GEOFENCE_KEY = "Into my ragged coat sleeves";
    private static final int PLACES_ACTIVITY_RESULT = 96;
    private GoogleApiClient mGoogleApiClient;

    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_geofence, container, false);
        mLatitudeEditText = (EditText) v.findViewById(R.id.latitudeEditText);
        mLongitudeEditText = (EditText) v.findViewById(R.id.longitudeEditText);
        v.findViewById(R.id.getLocationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(new PlacePicker.IntentBuilder().build(getActivity()),
                            PLACES_ACTIVITY_RESULT);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        v.findViewById(R.id.finishGeofenceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mGoogleApiClient.isConnected()) return;
                String latitude = mLatitudeEditText.getText().toString();
                String longitude = mLongitudeEditText.getText().toString();
                String radius = ((EditText) v.findViewById(R.id.radiusEditText)).getText().toString();
                if(latitude.length()<1 || longitude.length()<1){
                    Toast.makeText(getContext(), "Please enter valid LatLng data", Toast.LENGTH_SHORT).show();
                    return;
                }
                radius = radius.length() > 0 ? radius : "100";
                try {
                    startGeofencing(Double.parseDouble(latitude), Double.parseDouble(longitude),
                            Float.parseFloat(radius));
                } catch (NumberFormatException e){
                    Toast.makeText(getContext(), "Please enter valid LatLng data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        connectToPlayServices();
        return v;
    }

    private void connectToPlayServices(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
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
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//                .setLoiteringDelay(6000)
                .build());
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(fences);
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, builder.build(),
                getGeofencingPendingIntent()).setResultCallback(this);
        //Get location updates in order to make geofence work, remove if already there
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        updateLocation(this);
    }

    private void updateLocation(){
        LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mLatitudeEditText.setText(String.valueOf(location.getLatitude()));
                        mLongitudeEditText.setText(String.valueOf(location.getLongitude()));
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    }
                };
        updateLocation(locationListener);
    }

    private void updateLocation(LocationListener locationListener){
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            //This lil dude is asynchronous so we gotta back up for a sec and wait for him to do his magic
            //TODO: maybe updateLocation after/if we get permission isntead ohaving user click again
            return;
        }
        LocationRequest request = new LocationRequest();
        request.setInterval(LOCATION_UPDATE_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                request, locationListener);
    }

    private PendingIntent getGeofencingPendingIntent() {
        if (geoFencingPendingIntent == null) {
            Intent intent = new Intent(getContext(), GeofenceIntentService.class);
            geoFencingPendingIntent =
                    PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return geoFencingPendingIntent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PLACES_ACTIVITY_RESULT){
            if(resultCode== Activity.RESULT_OK){
                LatLng latLng = PlacePicker.getPlace(getContext(), data).getLatLng();
                startGeofencing(latLng.latitude, latLng.longitude, DEFAULT_RADIUS);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Google API", "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Google API", "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Google API", "Failed to connect");
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d("Geofence", "Success: " + status.getStatus());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", location.toString());
    }
}
