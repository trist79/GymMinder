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
     * the data. All data returned is a Workout or a list of Workouts.
     */
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseListener listener;

    public DbHelper(DatabaseListener listener) {
        this.listener = listener;
    }

    public void getTestUser() {
        database.child("users").child("1").addListenerForSingleValueEvent(new ValueEventListener() {
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
        database.child("users").child(user.getUid()).child(workoutName).setValue(workout);
    }

    /**
     * @param workoutName workout to be retrieved from database
     * @param user        owner of workout
     */
    public void retrieveWorkout(String workoutName, FirebaseUser user) {
        database.child("users").child(user.getUid()).child(workoutName).addListenerForSingleValueEvent(
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
        database.child("users").child(user.getUid())
                .child(day)
                .child(workoutName)
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
        database.child("users").child(user.getUid())
                .child(day)
                .child(workoutName)
                .setValue(workout);
    }

    /**
     * @param user user for which to retrieve all owned workouts
     */
    public void retrieveAllWorkouts(FirebaseUser user) {
        database.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Workout> workouts = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    workouts.add(ds.getValue(Workout.class));
                    Log.d("Database", "Workouts retrieved" + workouts.get(0));
                    listener.respondToWorkouts(workouts);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void testRetrieve() {
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
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

}
