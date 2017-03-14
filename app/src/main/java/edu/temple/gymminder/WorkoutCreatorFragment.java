package edu.temple.gymminder;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutCreatorFragment extends Fragment {
    BaseAdapter listAdapter;
    //TODO make a "builder" that builds exercises from the entered values here.
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
            public View getView(int position, View convertView, ViewGroup parent) {
                //TODO view reuse
                LinearLayout ll = new LinearLayout(getContext());
                ll.setOrientation(LinearLayout.HORIZONTAL);
//                EditText editTextExercise = new EditText(getContext());
                Spinner spinner = new Spinner(getContext());
                spinner.setAdapter(new BaseAdapter() {
                    String[] exercises = getResources().getStringArray(R.array.supported_exercises);
                    @Override
                    public int getCount() {
                        return exercises.length;
                    }

                    @Override
                    public Object getItem(int i) {
                        return exercises[i];
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
                EditText editTextSets = new EditText(getContext());
                EditText editTextReps = new EditText(getContext());
                editTextReps.setInputType(InputType.TYPE_CLASS_NUMBER);
                editTextSets.setInputType(InputType.TYPE_CLASS_NUMBER);
                editTextSets.setHint("Sets");
                editTextReps.setHint("Reps");
                ll.addView(spinner);
                ll.addView(editTextSets);
                ll.addView(editTextReps);
                return ll;
            }
        };

        ((ListView) v.findViewById(R.id.exercisesListview)).setAdapter(listAdapter);
        v.findViewById(R.id.addExerciseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exercises.add(new Exercise("w", 2, 3));
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
