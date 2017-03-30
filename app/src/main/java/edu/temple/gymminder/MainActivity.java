package edu.temple.gymminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements SigninFragment.SigninListener,
        MainFragment.DetailListener, WorkoutCreatorFragment.Listener {

    //TODO: Add sign-out button to options

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAuth();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                }
            }
        });
    }

    @Override
    public void goToMain() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, new MainFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToDetail(Workout workout, String name) {
        DetailFragment detailFragment = DetailFragment.newInstance(workout, name);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFrame, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToWorkoutCreator() {
        WorkoutCreatorFragment workoutCreatorFragment = new WorkoutCreatorFragment();
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
}
