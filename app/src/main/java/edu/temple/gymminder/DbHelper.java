package edu.temple.gymminder;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by rober_000 on 1/31/2017.
 */


public class DbHelper {
    /**
     * Utility class for interacting with firebase database. Any processing of retrieved objects
     * is handled by listener, which should be the calling component that wishes to operate on
     * the data. All data returned is a Workout or a List of Workouts.
     */
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private Listener listener;

    public DbHelper(Listener listener) {
        this.listener = listener;
    }

    public void getTestUser() {
        parsePath(WorkoutContract.TEST_GET_USER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Hey", dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * @param workout     workout data to be inserted
     * @param workoutName name of workout to act as identifier
     * @param user        owner of workout
     */
    public void addNewWorkout(Workout workout, String workoutName, FirebaseUser user) {
        parsePath(WorkoutContract.WORKOUTS, user.getUid(), workoutName).setValue(workout);
    }

    /**
     * @param workoutName workout to be retrieved from database
     * @param user        owner of workout
     */
    public void retrieveWorkout(String workoutName, FirebaseUser user) {
        parsePath(WorkoutContract.WORKOUTS, user.getUid(), workoutName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        listener.updateUi(dataSnapshot.getValue(Workout.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    /**
     * @param workoutName workout to be retrieved from database.
     * @param user        owner of workout
     * @param date        date workout was completed
     */
    public void retreieveWorkoutDate(String workoutName, FirebaseUser user, Date date) {
        String day = formatDateForWorkout(date);
        parsePath(WorkoutContract.DATED_WORKOUTS, user.getUid(), day, workoutName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        listener.updateUi(dataSnapshot.getValue(Workout.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * @param workout     workout to be inserted into database. All exercises must have concrete number
     *                    of completed reps
     * @param workoutName identifier of workout
     * @param user        owner of workout
     * @param date        date workout was completed
     */
    public void addWorkout(Workout workout, String workoutName, FirebaseUser user, Date date) {
        //Dated workouts are only for completed workouts
        for (int i = 0; i < workout.exercises.size(); i++) {
            if (workout.exercises.get(i).completed == null) return;
        }
        String day = formatDateForWorkout(date);
        parsePath(WorkoutContract.DATED_WORKOUTS, user.getUid(), day, workoutName).setValue(workout);
    }

    public void addWorkout(Workout workout, FirebaseUser user, Date date){
        //Unnamed workout for Workout builder
        for (int i = 0; i < workout.exercises.size(); i++) {
            if (workout.exercises.get(i).completed == null) return;
        }
        String day = formatDateForWorkout(date);
        parsePath(WorkoutContract.DATED_UNNAMED_WORKOUTS, user.getUid(), day).setValue(workout);
    }

    /**
     * @param user user for which to retrieve all owned workouts
     */
    public void retrieveAllWorkouts(FirebaseUser user) {
        parsePath(WorkoutContract.WORKOUTS, user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Workout> workouts = new ArrayList<>();
                //TODO maybe refactor workout definition to include name, so we don't have to do this
                ArrayList<String> names = new ArrayList<String>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    workouts.add(ds.getValue(Workout.class));
                    names.add(ds.getKey());
                    Log.d("Database", "Workout name: " + ds.getKey());
                    listener.respondToWorkouts(workouts, names);
                }
                Log.d("Database", "Workouts retrieved: " + workouts.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void testRetrieve() {
        parsePath(WorkoutContract.TEST_RETRIEVE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.updateUi(dataSnapshot.getValue(Workout.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * @param date date object to use for creating identifier
     * @return identifier for use in storing/retrieving completed workout in database
     */
    private String formatDateForWorkout(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_YEAR) + " " + cal.get(Calendar.YEAR);
    }

    private DatabaseReference parsePath(String[] path, String... strings) {
        DatabaseReference reference = database.getRoot();
        System.out.println(reference.toString());
        for (String s : path) {
            reference = reference.child(s);
        }
        for (String s : strings) {
            reference = reference.child(s);
        }
        return reference;
    }

    public static final class WorkoutContract {
        private static final String USERS = "users";
        private static final String DATED = "dated";
        private static final String STORED = "stored";
        private static final String UNNAMED = "unnamed";
        private static final String TEST = "1";

        public static final String[] DATED_WORKOUTS = {USERS, DATED};
        public static final String[] WORKOUTS = {USERS, STORED};
        public static final String[] TEST_RETRIEVE = {USERS};
        public static final String[] TEST_GET_USER = {USERS, TEST};
        public static final String[] DATED_UNNAMED_WORKOUTS = {USERS, UNNAMED};
    }

    public interface Listener {
        void updateUi(Workout workout);
        void respondToWorkouts(ArrayList<Workout> workouts, ArrayList<String> names);
    }

}
