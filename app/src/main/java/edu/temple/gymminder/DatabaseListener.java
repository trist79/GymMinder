package edu.temple.gymminder;

import java.util.ArrayList;

/**
 * Created by rober_000 on 1/31/2017.
 */
public interface DatabaseListener {
    void updateUi(Workout workout);
    void respondToWorkouts(ArrayList<Workout> workouts, ArrayList<String> names);
}
