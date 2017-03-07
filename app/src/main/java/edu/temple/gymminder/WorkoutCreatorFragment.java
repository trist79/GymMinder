package edu.temple.gymminder;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


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
            public View getView(int position, View convertView, ViewGroup parent) {
                //TODO view reuse
                LinearLayout ll = new LinearLayout(getContext());
                ll.setOrientation(LinearLayout.HORIZONTAL);
                EditText editText = new EditText(getContext());
                ll.addView(editText);
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
