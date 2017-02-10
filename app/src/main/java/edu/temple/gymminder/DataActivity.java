package edu.temple.gymminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rober_000 on 2/7/2017.
 */
public class DataActivity extends Activity {

    public final static String EXTRA_REPS_DONE = "We smoked the last one an hour ago";
    public final static String EXTRA_MAX_VELOCITY = "Cathy I'm lost";
    public final static String EXTRA_AVG_VELOCITY = "I don't know why";

    private ArrayList<ArrayList<Float>> data = new ArrayList<>(4);
    private ArrayList<Long> timestamps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for(int i=0;i<3;i++) data.add(new ArrayList<Float>());
        result(4, 5, 6);
    }

    void setupSensor(){
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        DataUtils.init(data, timestamps);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                DataUtils.process(event);
                //Get values, ignore low value movement
//                float x = Math.abs(event.values[0]) > 0.09 ? event.values[0] : 0;
//                float y = Math.abs(event.values[1]) > 0.09 ? event.values[1] : 0;
//                float z = Math.abs(event.values[2]) > 0.09 ? event.values[2] : 0;
//                timestamps.add(event.timestamp);
//                DataUtils.addUnfiltered(x, y, z);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    void result(int reps, int mv, int av){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REPS_DONE, reps);
        intent.putExtra(EXTRA_MAX_VELOCITY, mv);
        intent.putExtra(EXTRA_AVG_VELOCITY, av);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    void result(int reps){
        Intent intent = new Intent();
        float[] f = DataUtils.maxAndAvg(DataUtils.integrate(data.get(2)));
        intent.putExtra(EXTRA_REPS_DONE, reps);
        intent.putExtra(EXTRA_MAX_VELOCITY, f[0]);
        intent.putExtra(EXTRA_AVG_VELOCITY, f[1]);
        this.setResult(RESULT_OK, intent);
        finish();
    }



}
