package edu.temple.gymminder;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DatabaseListener {

    private FirebaseAuth auth;
    DbHelper dbHelper = new DbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAuth();
//
        dbHelper.getTestUser();
        ArrayList<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Dog", 5, 5));
        exercises.add(new Exercise("Cat", 4, 8));
        final Workout workout = new Workout(exercises);
        dbHelper.testRetrieve();
        final String username = "test1@gmail.com";
        final String password = "hawker";
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (auth.getCurrentUser() == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.mainFrame, new SigninFragment())
                    .commit();
            Log.d("Signup", "Dude");
            findViewById(R.id.signupButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    auth.createUserWithEmailAndPassword(username, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Auth", "Oh Yes");
                                        FirebaseUser user = auth.getCurrentUser();
                                        dbHelper.addNewWorkout(workout, "Seal", user);
                                    } else {
                                        Log.d("Auth", "Oh no");
                                    }
                                    Log.d("Auth", task.getResult().toString());

                                }
                            });
                }
            });
        } else {
            Log.d("Main", ""+auth.getCurrentUser().getEmail());
        }
    }

    @Override
    public void updateUi(Workout workout) {
        ((TextView)findViewById(R.id.testView)).setText(workout.toString());
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

}
