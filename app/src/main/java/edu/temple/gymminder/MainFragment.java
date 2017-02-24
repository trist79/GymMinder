package edu.temple.gymminder;


import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements DatabaseListener {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    DbHelper db = new DbHelper(this);
    DetailListener listener;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        db.retrieveAllWorkouts(auth.getCurrentUser());
        v.findViewById(R.id.add_workout_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.goToWorkoutCreator();
            }
        });
        ((TextView) v.findViewById(R.id.greeting)).setText(auth.getCurrentUser().getEmail());
        return v;
    }

    @Override
    public void updateUi(Workout workout) {

    }

    @Override
    public void respondToWorkouts(final ArrayList<Workout> workouts) {
        String res = "";
        for(Workout workout : workouts){
            res+=workout+"\n";
        }
        ((TextView) getView().findViewById(R.id.workouts)).setText(res);
        ListView lv = (ListView) getView().findViewById(R.id.workoutsList);
        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return workouts.size();
            }

            @Override
            public Object getItem(int position) {
                return workouts.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //TODO implement view reuse
                TextView tv = new TextView(getContext());
                tv.setText(getItem(position).toString());
                return tv;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.goToDetail((Workout) parent.getAdapter().getItem(position));
            }
        });
    }

    @Override
    public void onAttach(Context c){
        super.onAttach(c);
        listener = (DetailListener) c;
    }

    public interface DetailListener {
        void goToDetail(Workout workout);

        void goToWorkoutCreator();
    }

}
