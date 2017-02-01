package edu.temple.gymminder;

/**
 * Created by rober_000 on 1/31/2017.
 */

public class Exercise {
    public String workout;
    public int sets;
    public int reps;

    public Exercise(){

    }

    public Exercise(String workout, int sets, int reps){
        this.workout = workout;
        this.sets = sets;
        this.reps = reps;
    }
}
