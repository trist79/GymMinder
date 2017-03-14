package edu.temple.gymminder;


import android.content.Context;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutCreatorFragment extends Fragment {

    BaseAdapter listAdapter;
    ArrayList<Exercise> exercises = new ArrayList<>();
    Listener listener;
    //TODO probably refactor this to something that makes more sense o3o
    WorkoutCreatorFragment self = this;

    public WorkoutCreatorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_workout_creator, container, false);
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
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.item_exercise_creator,
                        parent, false);

                final Spinner spinner = (Spinner) ll.findViewById(R.id.exerciseSpinner);
                spinner.setAdapter(new BaseAdapter() {
                    String[] exerciseNames = getResources().getStringArray(R.array.supported_exercises);
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
                        //TODO make this level with EditTexts
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
                if(!exercise.workout.equals("w")){
                    //TODO maybe refactor exercise strings into dictionary to make this niftier
                    //TODO also make string array/dict accessible here :d
                    int selection = 0;
                    if(exercise.workout.equals("Bench Press")){
                        selection = 0;
                    } else if (exercise.workout.equals("Deadlift")){
                        selection = 2;
                    } else {
                        selection = 1;
                    }
                    spinner.setSelection(selection);
                }
                EditText editTextSets = (EditText) ll.findViewById(R.id.setsEditText);
                editTextSets.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        try {
                            exercise.sets = Integer.parseInt(editable.toString());
                        } catch (Exception e){
                            //TODO error checking
                        }
                    }
                });
                EditText editTextReps = (EditText) ll.findViewById(R.id.repsEditText);
                editTextReps.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        try {
                            exercise.reps = Integer.parseInt(editable.toString());
                        } catch (Exception e){
                            //TODO error checking
                        }
                    }
                });
                if(exercise.sets != -999){
                    editTextSets.setText(String.valueOf(exercise.sets));
                }
                if(exercise.reps != -999){
                    ((EditText) ll.findViewById(R.id.repsEditText)).setText(String.valueOf(exercise.reps));
                }
                return ll;
            }
        };

        ((ListView) v.findViewById(R.id.exercisesListview)).setAdapter(listAdapter);
        v.findViewById(R.id.addExerciseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exercises.add(new Exercise("w", -999, -999));
                listAdapter.notifyDataSetChanged();
            }
        });
        v.findViewById(R.id.finishExerciseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbHelper dbHelper = new DbHelper(null);
                dbHelper.addNewWorkout(new Workout(exercises), "8^)", FirebaseAuth.getInstance().getCurrentUser());
                listener.finishFragment(self);
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context c){
        super.onAttach(c);
        listener = (Listener) c;
    }

    public interface Listener{
        void finishFragment(Fragment f);
    }

}
