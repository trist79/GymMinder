package edu.temple.gymminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;

import java.util.ArrayList;

/**
 * Handles vibration on reps, calling DataUtils methods to handle data, accelerometer recording,
 * responding to calling component with relevant data such as number of reps, stream stats, the
 * stream array, etc.
 */
public class DataActivity extends Activity implements DataUtils.Listener {

    public final static String EXTRA_REPS_DONE = "We smoked the last one an hour ago";
    public final static String EXTRA_MAX_VELOCITY = "Cathy I'm lost";
    public final static String EXTRA_AVG_VELOCITY = "I don't know why";

    private ArrayList<ArrayList<Float>> data = new ArrayList<>(4);
    private ArrayList<Long> timestamps = new ArrayList<>();
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        for (int i = 0; i < 3; i++) data.add(new ArrayList<Float>());
        result(4, 5, 6);
    }

    void setupSensor() {
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        DataUtils.init(data, timestamps);
        DataUtils.setListener(this);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                DataUtils.process(event.values, event.timestamp);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sensor, 10000);
    }


    void result(int reps, int mv, int av) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REPS_DONE, reps);
        intent.putExtra(EXTRA_MAX_VELOCITY, mv);
        intent.putExtra(EXTRA_AVG_VELOCITY, av);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    void result(int reps) {
        Intent intent = new Intent();
        float[] f = DataUtils.maxAndAvg(DataUtils.riemann(data.get(2)));
        intent.putExtra(EXTRA_REPS_DONE, reps);
        intent.putExtra(EXTRA_MAX_VELOCITY, f[0]);
        intent.putExtra(EXTRA_AVG_VELOCITY, f[1]);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Prevent memory leak
        DataUtils.removeListener();
    }

    @Override
    public void respondToRep() {
        vibrator.vibrate(100);
    }
}
