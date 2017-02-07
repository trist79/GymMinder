package edu.temple.gymminder;

/**
 * Created by rober_000 on 1/31/2017.
 */

public class Exercise {
    public String workout;
    public int sets;
    public int reps;
    public int[] completed = null;
    public int setsDone;

    public Exercise(){

    }

    public Exercise(String workout, int sets, int reps){
        this.workout = workout;
        this.sets = sets;
        this.reps = reps;
    }

    public Exercise(Exercise exercise){
        workout = exercise.workout;
        sets = exercise.sets;
        reps = exercise.reps;
        initActive();
    }

    void initActive(){
        completed = new int[sets];
        for(int i=0;i<sets;i++) completed[i] = -1;
        setsDone = 0;
    }



}
