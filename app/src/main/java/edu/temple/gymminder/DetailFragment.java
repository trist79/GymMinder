package edu.temple.gymminder;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {


    public DetailFragment() {
        // Required empty public constructor
    }

    public static DetailFragment newInstance(Workout workout){
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("workout", workout);
        detailFragment.setArguments(args);
        return detailFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        Workout workout = (Workout) getArguments().getSerializable("workout");
        if(workout == null) return v;
        ((TextView) v.findViewById(R.id.textView)).setText(workout.toString());

        final ArrayList<Exercise> exercises = workout.exercises;
        ListView lv = (ListView) v.findViewById(R.id.workoutsList);
        lv.setAdapter(new BaseAdapter() {
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

                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final Exercise exercise = (Exercise) getItem(position);
                if(exercise.completed==null) exercise.initActive();
                View item = inflater.inflate(R.layout.item_exercise, parent, false);
                ((TextView) item.findViewById(R.id.workoutName)).setText(exercise.workout);
                final TextView setProgress = (TextView) item.findViewById(R.id.setNumber);
                setProgress.setText(getString(R.string.sets_progress, 0, exercise.sets));
                item.findViewById(R.id.startTrackerButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(exercise.setsDone >= exercise.sets) return;
                        setProgress.setText(getString(R.string.sets_progress,
                                ++exercise.setsDone, exercise.sets));
                    }
                });
                return item;
            }
        });
        return v;
    }

}
