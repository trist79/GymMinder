package edu.temple.gymminder;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Created by rober_000 on 4/11/2017.
 */

public class DbHelperStub extends DbHelper {


    public DbHelperStub(Listener listener) {
        super(listener);
    }



    @Override
    public void retrieveAllWorkouts(FirebaseUser user) {
        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Bench", 3, 3));
        workouts.add(new Workout(exercises));
        ArrayList<String> names = new ArrayList<>();
        names.add("hey p3p");
        listener.respondToWorkouts(workouts, names);
    }

    @Override
    public void getCatalog() {
        listener.updateUi(null);
    }

}
