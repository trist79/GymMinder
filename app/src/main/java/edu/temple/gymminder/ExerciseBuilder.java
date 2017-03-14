package edu.temple.gymminder;

import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by rober_000 on 3/13/2017.
 */

public class ExerciseBuilder {
    private Spinner workout;
    private EditText sets;
    private EditText reps;

    public ExerciseBuilder(){

    }

    public boolean isInit(){
        return sets != null;
    }

    public Workout finish(ArrayList<ExerciseBuilder> exerciseBuilders){
        ArrayList<Exercise> exercises = new ArrayList<>(exerciseBuilders.size());
        for(ExerciseBuilder eb : exerciseBuilders){
            Exercise e = eb.build();
            if(e!=null) exercises.add(e);
        }
        return new Workout(exercises);
    }

    private Exercise build(){
        //TODO add real error checking and return null if any position causes problems
        System.out.println("y"+sets.getText().toString());
        System.out.println("x"+reps.getText().toString());
        System.out.println("z"+workout.getSelectedItem().toString());
        try {
            return new Exercise(workout.getSelectedItem().toString(),
                    Integer.parseInt(sets.getText().toString()),
                    Integer.parseInt(reps.getText().toString()));
        } catch (Exception e){
            System.out.println(e.toString());
            return null;
        }
    }

    public void init(Spinner workout, EditText sets, EditText reps){
        this.workout = workout;
        this.sets = sets;
        this.reps = reps;
    }
}