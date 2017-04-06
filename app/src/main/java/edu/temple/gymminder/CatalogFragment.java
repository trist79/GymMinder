package edu.temple.gymminder;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment implements DbHelper.Listener {


    private static final String TAG = "CatalogFragment";

    public CatalogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_catalog, container, false);
        DbHelper helper = new DbHelper(this);
        helper.getCatalog();
        return v;
    }

    @Override
    public void updateUi(Workout workout) {
        ViewGroup v = (ViewGroup) getView();
        if(v!=null) {
            Log.d(TAG, "Writing workouts");
            for (Exercise e : workout.exercises) {
                TextView t = new TextView(getContext());
                t.setText(e.workout);
                v.addView(t);
            }
        } else {
            Log.d(TAG, "getView() returned null");
        }
    }

    @Override
    public void respondToWorkouts(ArrayList<Workout> workouts, ArrayList<String> names) {

    }
}
