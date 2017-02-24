package edu.temple.gymminder;


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

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutCreatorFragment extends Fragment {
    BaseAdapter listAdapter;
    ArrayList<Exercise> exercises = new ArrayList<>();

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
        return v;
    }

}
