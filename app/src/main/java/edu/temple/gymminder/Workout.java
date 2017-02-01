package edu.temple.gymminder;

import java.util.List;

/**
 * Created by rober_000 on 1/31/2017.
 */

public class Workout {
    List<Exercise> exercises;

    public Workout(){

    }

    public Workout(List<Exercise> exercises){
        this.exercises = exercises;
    }

    @Override
    public String toString(){
        String res = "";
        for(Exercise e : exercises){
            res+= e.workout +": " + e.reps + "x" + e.sets+ "\n";
        }
        res = res.substring(0, res.length()-1);
        return res;
    }
}
