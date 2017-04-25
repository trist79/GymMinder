package edu.temple.gymminder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExerciseDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExerciseDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciseDataFragment extends Fragment implements DataUtils.Listener {

    private static final String ARG_EXERCISE = "ARG_EXERCISE";

    private Exercise mExercise;
    private ArrayList<ArrayList<Float>> data = new ArrayList<>(4);
    private ArrayList<Long> timestamps = new ArrayList<>();
    private int mReps = 0;
    private Vibrator vibrator;

    private TextView mRepsTextView;
    private ProgressBar mSetProgressBar;

    private OnFragmentInteractionListener mListener;

    public ExerciseDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param exercise The current exercise
     * @return A new instance of fragment ExerciseDataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExerciseDataFragment newInstance(Exercise exercise) {
        ExerciseDataFragment fragment = new ExerciseDataFragment();
        Bundle args = new Bundle();
        args.getSerializable(ARG_EXERCISE);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mExercise = (Exercise) getArguments().getSerializable(ARG_EXERCISE);
        }

        vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        for (int i = 0; i < 3; i++) data.add(new ArrayList<Float>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_exercise_data, container, false);

        Button button = (Button) v.findViewById(R.id.finish_set_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed();
            }
        });

        mRepsTextView = (TextView) v.findViewById(R.id.rep_text_view);
        mSetProgressBar = (ProgressBar) v.findViewById(R.id.setProgressBar);

        return v;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.didFinish(0, 0, 0, null);
        }
    }

    void setupSensor() {
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        DataUtils.init(data, timestamps);
//        DataUtils.loadRepetitionFile(mExercise.name, getActivity());
        DataUtils.setListener(this);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                DataUtils.process(event.values, event.timestamp);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        }, sensor, 10000);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        DataUtils.removeListener();
    }

    @Override
    public void respondToRep() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (vibrator.hasVibrator())
                    vibrator.vibrate(100);

                mReps++;
                mRepsTextView.setText(mReps + "");
            }
        });

    }

    public interface OnFragmentInteractionListener {
        void didFinish(int reps, int mv, int av, ArrayList<ArrayList<Float>> data);
    }
}
