package edu.temple.gymminder;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rober_000 on 1/31/2017.
 */

public class Exercise implements Serializable {
    public String workout;
    public int sets;
    public int reps;
    public ArrayList<Integer> completed = null;
    public int setsDone;

    public Exercise() {

    }

    public Exercise(String workout, int sets, int reps) {
        this.workout = workout;
        this.sets = sets;
        this.reps = reps;
    }

    public Exercise(String workout, int sets, int reps, ArrayList<Integer> completed, int setsDone) {
        this.workout = workout;
        this.sets = sets;
        this.reps = reps;
        this.completed = completed;
        this.setsDone = setsDone;
    }

    public Exercise(Exercise exercise) {
        workout = exercise.workout;
        sets = exercise.sets;
        reps = exercise.reps;
        initActive();
    }

    void initActive() {
        completed = new ArrayList<>(sets);
        for (int i = 0; i < sets; i++) completed.add(-1);
        setsDone = 0;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setWorkout(String workout) {
        this.workout = workout;
    }

    @Override
    public String toString() {
        return workout + ": " + reps + "x" + sets;
    }

}
