package edu.temple.gymminder;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static edu.temple.gymminder.DetailFragment.EXTRA_WORKOUT; //TODO refactor to DataActivity


/**
 * A simple {@link Fragment} subclass.
 */
public class AdHocCreatorFragment extends Fragment implements DbHelper.Listener{

    public static final int RESULT_REPS = 7073;

    BaseAdapter listAdapter;
    ArrayList<Exercise> exercises = new ArrayList<>();
    String[] exerciseNames;
    Listener listener;
    String PLACEHOLDER_STRING = "I know that" +
            "It's hard" +
            "But you have" +
            "To try" +
            ":)" +
            "8)" +
            "8^)" +
            "d8^)" +
            "d8^)c";
    //TODO maybe refactor this to something that makes more sense o3o
    AdHocCreatorFragment self = this;

    public AdHocCreatorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ad_hoc_creator, container, false);
        if(exercises.size()==0) exercises.add(new Exercise(":^)", -99, -99));
        listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return exercises.size();
            }

            @Override
            public Object getItem(int position) {
                return exercises.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                //TODO view reuse
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout ll;
                if(position == getCount()-1) {
                   ll = (LinearLayout) inflater.inflate(R.layout.item_ad_hoc_creator,
                            parent, false);
                    final Spinner spinner = (Spinner) ll.findViewById(R.id.exerciseSpinner);
                    spinner.setAdapter(new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return exerciseNames.length;
                        }

                        @Override
                        public Object getItem(int i) {
                            return exerciseNames[i];
                        }

                        @Override
                        public long getItemId(int i) {
                            return i;
                        }

                        @Override
                        public View getView(int i, View view, ViewGroup viewGroup) {
                            LinearLayout ll = new LinearLayout(getContext());
                            TextView textView = new TextView(getContext());
                            textView.setText(getItem(i).toString());
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                            ll.addView(textView);
                            return ll;
                        }
                    });
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            Exercise ex = (Exercise) listAdapter.getItem(position); //refers to ListView
                            String workout = (String) spinner.getAdapter().getItem(i); //refers to Spinner
                            ex.setWorkout(workout);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    final Exercise exercise = (Exercise) getItem(position);
                    if (!exercise.workout.equals(PLACEHOLDER_STRING)) {
                        for (int i = 0; i < exerciseNames.length; i++) {
                            if (exercise.workout.equals(exerciseNames[i])) {
                                spinner.setSelection(i);
                                break;
                            }
                        }
                    }
                    ll.findViewById(R.id.addExerciseButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            exercises.add(new Exercise(PLACEHOLDER_STRING, -999, -999));
                            listAdapter.notifyDataSetChanged();
                            Intent intent = new Intent(getContext(), DataActivity.class);
                            intent.putExtra(EXTRA_WORKOUT, exercise.workout);
                            startActivityForResult(intent, RESULT_REPS);
                        }
                    });
                } else {
                    ll = (LinearLayout) inflater.inflate(R.layout.item_ad_hoc_completed,
                            parent, false);
                    ((TextView) ll.findViewById(R.id.exerciseTextView)).setText(
                            ((Exercise) getItem(position)).workout);
                    ((TextView) ll.findViewById(R.id.repsTextView)).setText(
                            getString(R.string.workout_reps_display, ((Exercise) getItem(position)).reps));
                }
                return ll;
            }
        };

        ((ListView) v.findViewById(R.id.exercisesListview)).setAdapter(listAdapter);
        v.findViewById(R.id.finishWorkoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Exercise> newExercises = new ArrayList<>();
                for(Exercise e : exercises){
                    if(e.reps > 0) newExercises.add(e);
                }
                if(newExercises.size() > 0){
                    DbHelper.newInstance(null).addWorkout(new Workout(newExercises), "ad hoc",
                            FirebaseAuth.getInstance().getCurrentUser(),
                            Calendar.getInstance().getTime());
                }
                listener.finishFragment(self);
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == RESULT_REPS){
            if(resultCode == Activity.RESULT_OK){
                int repsDone = data.getIntExtra(DataActivity.EXTRA_REPS_DONE, -1);
                Exercise exercise = exercises.get(exercises.size()-2);
                exercise.reps = repsDone;
                exercise.sets = 1;
                exercise.setsDone = 1;
                exercise.completed = new ArrayList<>(Arrays.asList(new Integer[] {repsDone}));
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        exerciseNames = getResources().getStringArray(R.array.supported_exercises);
        listener = (Listener) c;
    }

    @Override
    public void updateUi(Workout workout) {

    }

    @Override
    public void respondToWorkouts(ArrayList<Workout> workouts, ArrayList<String> names) {

    }

    @Override
    public void onWorkoutAdded() {

    }

    public interface Listener {
        void finishFragment(Fragment f);
    }

}
