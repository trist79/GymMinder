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
import java.util.Calendar;

import static edu.temple.gymminder.DetailFragment.EXTRA_WORKOUT; //TODO refactor


/**
 * A simple {@link Fragment} subclass.
 */
public class AdHocCreatorFragment extends Fragment {

    public static final int RESULT_REPS = 7073;

    BaseAdapter listAdapter;
    ArrayList<Exercise> exercises = new ArrayList<>();
    String[] exerciseNames;
    Listener listener;
    String PLACEHOLDER_STRING = "You may be surprised to hear that when the revolution happens," +
            "the proletariat will be destroyed. Indeed, just as the bourgeois will be stripped" +
            "of power and erased, so too will the proletariat. After the revolution, there will" +
            "be no proletariat, and there will be no bourgeois. There will only be people." +
            "So rise up, comrades! And take what is yours!";
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
        exercises.add(new Exercise(":^)", -99, -99));
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
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.item_ad_hoc_creator,
                        parent, false);
                //TODO: make spinner accessible only if it's the most recent workout. Other workouts should be a textview
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
                //TODO: maybe set a listener so we can exit after it uploads
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == RESULT_REPS){
            if(resultCode == Activity.RESULT_OK){
                int repsDone = data.getIntExtra(DataActivity.EXTRA_REPS_DONE, -1);
                exercises.get(exercises.size()-2).reps = repsDone;
                exercises.get(exercises.size()-2).completed = new ArrayList<>();//TODO temp to get it to write by not being null, change later
            }
        }
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        exerciseNames = getResources().getStringArray(R.array.supported_exercises);
        listener = (Listener) c;
    }

    public interface Listener {
        void finishFragment(Fragment f);
    }

}
