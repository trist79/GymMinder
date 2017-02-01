package edu.temple.gymminder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements DatabaseListener, SigninListener {

    private FirebaseAuth auth;
    DbHelper dbHelper = new DbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAuth();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (auth.getCurrentUser() != null) {
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
    public void updateUi(Workout workout) {
//        ((TextView)findViewById(R.id.testView)).setText(workout.toString());
    }

    public void setupAuth(){
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
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
                .commit();
    }
}
