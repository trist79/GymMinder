package edu.temple.gymminder;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by rober_000 on 1/31/2017.
 */



public class DbHelper {
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseListener listener;

    public DbHelper(DatabaseListener listener){
        this.listener = listener;
    }

    public void getTestUser(){
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


    public void addNewWorkout(Workout workout, String workoutName, FirebaseUser user){
        database.child("users").child(String.valueOf(user.getUid())).child(workoutName).setValue(workout);
    }

    public void retrieveWorkout(String workoutName, FirebaseUser user){
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

    public void retrieveAllWorkouts(FirebaseUser user){
        database.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Workout> workouts = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    workouts.add(ds.getValue(Workout.class));
                    listener.respondToWorkouts(workouts);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void testRetrieve(){
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
}
