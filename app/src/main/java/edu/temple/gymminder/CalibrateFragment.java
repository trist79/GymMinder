package edu.temple.gymminder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalibrateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalibrateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrateFragment extends Fragment {

    private static final String EXERCISE_ARG = "ARG_EXERCISE";

    private enum ButtonState {
        START, STOP, REDO
    }

    private OnFragmentInteractionListener mListener;

    private String mExerciseName;
    private TimeSeriesBase.Builder mXTimeSeriesBuilder, mYTimeSeriesBuilder, mZTimeSeriesBuilder;

    // UI
    private ProgressBar mProgessBar;
    private Button mButton;
    private ButtonState mButtonState = ButtonState.START;

    public CalibrateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param exerciseName Parameter 1.
     * @return A new instance of fragment CalibrateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalibrateFragment newInstance(String exerciseName) {
        CalibrateFragment fragment = new CalibrateFragment();
        Bundle args = new Bundle();
        args.putString(EXERCISE_ARG, exerciseName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mExerciseName = getArguments().getString(EXERCISE_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calibrate, container, false);

        mXTimeSeriesBuilder = TimeSeriesBase.builder();
        mYTimeSeriesBuilder = TimeSeriesBase.builder();
        mZTimeSeriesBuilder = TimeSeriesBase.builder();

        mProgessBar = (ProgressBar) v.findViewById(R.id.calibrate_progess);
        mButton = (Button) v.findViewById(R.id.calibrate_button);
        if (mButton != null) {
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onButtonPressed();
                }
            });
        }

        return v;
    }

    public void onButtonPressed() {

        switch (mButtonState) {
            case START:
                mButton.setText(R.string.done_calibration);
                mButtonState = ButtonState.STOP;
                break;
            case STOP:
                mButton.setText(R.string.redo_calibration);
                mButtonState = ButtonState.REDO;
                break;
            default:
                break;
        }

        // Animate and show the progress bar
        mProgessBar.animate();
        mProgessBar.setVisibility(View.VISIBLE);

        TimeSeries xTimeSeries = mXTimeSeriesBuilder.build();
        TimeSeries yTimeSeries = mYTimeSeriesBuilder.build();
        TimeSeries zTimeSeries = mZTimeSeriesBuilder.build();

        // TODO: Start recording, process the data, and write to a file
        File f = new File(getContext().getCacheDir(), mExerciseName + "_calibration.dat");
        if (mListener != null) {
            mListener.onCalibrationComplete(Uri.fromFile(f), 0);
        }
    }

    void setupSensor() {
        SensorManager sm = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mXTimeSeriesBuilder.add(event.timestamp, event.values[0]);
                mYTimeSeriesBuilder.add(event.timestamp, event.values[1]);
                mZTimeSeriesBuilder.add(event.timestamp, event.values[2]);
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

    public interface OnFragmentInteractionListener {

        /**
         * Called when the fragment has finished the calibration process.
         * @param uri The uri to the file containing the data of the processed time series recorded during calibration
         * @param majorAxisIndex The index of the detected major axis
         */
        void onCalibrationComplete(Uri uri, Integer majorAxisIndex);
    }
}
