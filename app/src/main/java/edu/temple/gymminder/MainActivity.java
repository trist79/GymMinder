package edu.temple.gymminder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SigninFragment.SigninListener,
        MainFragment.DetailListener, WorkoutCreatorFragment.Listener, AdHocCreatorFragment.Listener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private PendingIntent geoFencingPendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth auth;
    private static final long LOCATION_UPDATE_INTERVAL = BuildConfig.DEBUG ? 5000 : 60000;
    private static final String GEOFENCE_KEY = "Into my ragged coat sleeves";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAuth();
        if (BuildConfig.FLAVOR.equals("espresso")) {
            auth.signOut();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (auth.getCurrentUser() == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.mainFrame, new SigninFragment())
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.mainFrame, new MainFragment())
                    .commit();
        }
        Log.d("W", "What");
        connectToPlayServices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.signOutOption) {
            auth.signOut();
        }
        return true;
    }

    public void setupAuth() {
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("Auth", "Signed in");
                } else {
                    Log.d("Auth", "Not Signed In");
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                            new SigninFragment()).commit();
                }
            }
        });
    }

    public void goToMain() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, new MainFragment())
                .addToBackStack(null)
                .commit();
    }

    public void goToDetail(Workout workout, String name) {
        DetailFragment detailFragment = DetailFragment.newInstance(workout, name);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    public void goToWorkoutCreator() {
        WorkoutCreatorFragment workoutCreatorFragment = new WorkoutCreatorFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, workoutCreatorFragment)
                .addToBackStack(null)
                .commit();
    }

    public void goToAdHocCreator() {
        AdHocCreatorFragment workoutCreatorFragment = new AdHocCreatorFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, workoutCreatorFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void finishFragment(Fragment f) {
        getSupportFragmentManager().popBackStack();
    }

    public void goToChart(float[] accelerationStream) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, ChartFragment.newInstance(accelerationStream))
                .addToBackStack(null)
                .commit();
    }

    private PendingIntent getGeofencingPendingIntent() {
        if (geoFencingPendingIntent == null) {
            Intent intent = new Intent(this, GeofenceIntentService.class);
            geoFencingPendingIntent =
                    PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return geoFencingPendingIntent;
    }

    private void startGeofencing(double latitude, double longitude) {
        ArrayList<Geofence> fences = new ArrayList<>();
        fences.add(new Geofence.Builder()
                .setRequestId(GEOFENCE_KEY)
                .setCircularRegion(latitude, longitude, 1000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//                .setLoiteringDelay(6000)
                .build());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(fences);
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, builder.build(),
                getGeofencingPendingIntent()).setResultCallback(this);
        LocationRequest request = new LocationRequest();
        request.setInterval(LOCATION_UPDATE_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }
                });
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Google API", "Connected");
        startGeofencing(10, 10);
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
}
