package edu.temple.gymminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.temple.gymminder.geofence.GeofenceFragment;

public class MainActivity extends AppCompatActivity
        implements SigninFragment.SigninListener,
        WorkoutsFragment.DetailListener,
        WorkoutCreatorFragment.Listener,
        AdHocCreatorFragment.Listener,
        AccountFragment.OnFragmentInteractionListener {

    public static final String AD_HOC = "Laughing to the bank like ahhHA";
    public static final String START_FRAGMENT_EXTRA = "It was always me vs the world." +
            "Until I found it was me vs me.";

    private FirebaseAuth auth;
    private Fragment activeFragment;
    private BottomNavigationView tabBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAuth();

        tabBar = (BottomNavigationView) findViewById(R.id.navigation);
        tabBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_workouts:
                        goToWorkouts();
                        return true;
                    case R.id.navigation_history:
                        goToHistory();
                        return true;
                    case R.id.navigation_account:
                        goToAccount();
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (BuildConfig.FLAVOR.equals("espresso")) {
            auth.signOut();
            DataUtils.loadRepetitionFile("Bench", this).delete();
        }
        if (auth.getCurrentUser() == null) {
            startFragment(new SigninFragment());
        } else if (getIntent().getExtras()!=null) {
            if (getIntent().getExtras().get(START_FRAGMENT_EXTRA)!=null){
                handleStartFragmentExtra(getIntent().getExtras());
            }
        } else {
            goToWorkouts();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras!=null) {
            if(extras.get(START_FRAGMENT_EXTRA)!=null) {
                handleStartFragmentExtra(extras);
            }
        }
    }

    private void handleStartFragmentExtra(Bundle extras){
        String fragment = extras.getString(START_FRAGMENT_EXTRA, "");
        switch (fragment) {
            case AD_HOC:
                startFragment(new AdHocCreatorFragment());
                break;
            default:
                startFragment(new WorkoutsFragment());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.signOutOption:
                auth.signOut();
                break;

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
                    startFragment(new SigninFragment());
                }
            }
        });
    }

    public void goToWorkouts() {
        startFragment(new WorkoutsFragment());
        tabBar.setVisibility(View.VISIBLE);
        tabBar.getMenu().findItem(R.id.navigation_workouts).setChecked(true);
    }

    public void goToWorkoutsDetail(Workout workout, String name) {
        DetailFragment detailFragment = DetailFragment.newInstance(workout, name);
        startFragment(detailFragment);
    }

    public void goToWorkoutCreator() {
        WorkoutCreatorFragment workoutCreatorFragment = new WorkoutCreatorFragment();
        startFragment(workoutCreatorFragment);
    }

    public void goToHistory() {
        tabBar.setVisibility(View.VISIBLE);
        tabBar.getMenu().findItem(R.id.navigation_history).setChecked(true);
    }

    public void goToAccount() {
        startFragment(new AccountFragment());
        tabBar.setVisibility(View.VISIBLE);
        tabBar.getMenu().findItem(R.id.navigation_account).setChecked(true);
    }


    @Override
    public void finishFragment(Fragment f) {
        getSupportFragmentManager().popBackStack();
    }


    public void startFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, fragment);
        if(activeFragment instanceof WorkoutsFragment) transaction = transaction.addToBackStack(null);
        transaction.commit();
        activeFragment = fragment;
    }


    @Override
    public void onSignOutPressed() {
        tabBar.setVisibility(View.GONE);
        auth.signOut();
    }
}
