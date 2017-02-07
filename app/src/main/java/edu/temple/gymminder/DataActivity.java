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

/**
 * Created by rober_000 on 2/7/2017.
 */
public class DataActivity extends Activity {

    public final static String EXTRA_REPS_DONE = "We smoked the last one an hour ago";
    public final static String EXTRA_MAX_VELOCITY = "Cathy I'm lost";
    public final static String EXTRA_AVG_VELOCITY = "I don't know why";

    private ArrayList<LinkedList<Float>> data = new ArrayList<>(4);
    private LinkedList<Long> timestamps = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for(int i=0;i<3;i++) data.add(new LinkedList<Float>());
        result(4, 5, 6);
    }

    void setupSensor(){
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(new SensorEventListener() {
            float mx = 0;
            float my = 0;
            float mz = 0;

            @Override
            public void onSensorChanged(SensorEvent event) {
                //Get values, ignore low value movement
                float x = Math.abs(event.values[0]) > 0.09 ? event.values[0] : 0;
                float y = Math.abs(event.values[1]) > 0.09 ? event.values[1] : 0;
                float z = Math.abs(event.values[2]) > 0.09 ? event.values[2] : 0;
                //Get max acceleration
                mx = Math.abs(mx) > Math.abs(x) ? mx : x;
                my = Math.abs(my) > Math.abs(y) ? my : y;
                mz = Math.abs(mz) > Math.abs(z) ? mz : z;
                timestamps.add(event.timestamp);
                addUnfiltered(x, y, z);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void addWithLowPassFilter(float x, float y, float z, float alpha){
        if(data.get(0).size() > 0) {
            float ox = data.get(0).getLast();
            float oy = data.get(1).getLast();
            float oz = data.get(2).getLast();
            data.get(0).addLast(ox + alpha * (x - ox));
            data.get(1).addLast(oy + alpha * (y - oy));
            data.get(2).addLast(oz + alpha * (z - oz));
        } else {
            addUnfiltered(x, y, z);
        }
    }


    void addUnfiltered(float x, float y, float z){
        data.get(0).addLast(x);
        data.get(1).addLast(y);
        data.get(2).addLast(z);
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
        float[] f = maxAndAvg(integrate(data.get(2)));
        intent.putExtra(EXTRA_REPS_DONE, reps);
        intent.putExtra(EXTRA_MAX_VELOCITY, f[0]);
        intent.putExtra(EXTRA_AVG_VELOCITY, f[1]);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    float[] integrate(LinkedList<Float> list){
        float conversion = 1.0f / 1000000000.0f;
        float[] velocity = new float[data.get(0).size()-1];
        Iterator<Float> iterator = list.listIterator();
        int i = 0;
        while(iterator.hasNext() && timestamps.size() > i+1){
            velocity[i] = iterator.next() * (timestamps.get(i+1) - timestamps.get(i)) * conversion;
            i++;
        }
        return velocity;
    }

    float[] maxAndAvg(float[] floats){
        float f[] = new float[2];
        float max = floats[0];
        float sum = 0;
        for(int i=0;i<floats.length;i++){
            max = max > floats[i] ? max : floats[i];
            sum+=floats[i];
        }
        f[0] = max;
        f[1] = sum / floats.length;
        return f;
    }


}
