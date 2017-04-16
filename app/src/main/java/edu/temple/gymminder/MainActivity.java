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
import android.widget.Toast;

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
        MainFragment.DetailListener, WorkoutCreatorFragment.Listener, AdHocCreatorFragment.Listener{

    public static final String AD_HOC = "Laughing to the bank like ahhHA";
    public static final String START_FRAGMENT_EXTRA = "It was always me vs the world." +
            "Until I found it was me vs me.";

    private FirebaseAuth auth;

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
            goToMain();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.mainFrame, new GeofenceFragment())
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras!=null) {
            if(extras.get(START_FRAGMENT_EXTRA)!=null) {
                String fragment = extras.getString(START_FRAGMENT_EXTRA, "");
                switch (fragment) {
                    case AD_HOC:
                        goToAdHocCreator();
                        return;
                    default:
                        goToMain();
                }
            }
        }
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

    public void goToGeofence(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, new GeofenceFragment())
                .addToBackStack(null)
                .commit();
    }


}
