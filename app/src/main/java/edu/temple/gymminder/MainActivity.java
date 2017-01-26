package edu.temple.gymminder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    String result1 = "";
    String result2 = "";
    String result3 = "";
    TextView mTextView1;
    TextView mTextView2;
    TextView mTextView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView1 = (TextView) findViewById(R.id.values1);
        mTextView2 = (TextView) findViewById(R.id.values2);
        mTextView3 = (TextView) findViewById(R.id.values3);
        View bt = findViewById(R.id.startButton);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                sm.registerListener(new SensorEventListener() {
                    float mx = 0;
                    float my = 0;
                    float mz = 0;

                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float x = Math.abs(event.values[0]) > 0.09 ? event.values[0] : 0;
                        float y = Math.abs(event.values[1]) > 0.09 ? event.values[1] : 0;
                        float z = Math.abs(event.values[2]) > 0.09 ? event.values[2] : 0;
                        mx = mx > x ? mx : x;
                        my = my > y ? my : y;
                        mz = mz > z ? mz : z;
                        mTextView1.setText(String.valueOf(x) +" " + String.valueOf(mx));
                        mTextView2.setText(String.valueOf(y) +" " + String.valueOf(my));
                        mTextView3.setText(String.valueOf(z) +" " + String.valueOf(mz));
                        result1+=x;
                        result2+=y;
                        result3+=z;
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                }, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

    }

    @Override
    protected void onDestroy(){
        String result = result1+"\n"+result2+"\n"+result3;
        File f = new File(getFilesDir(), "test");
        try {
            FileOutputStream fout = new FileOutputStream(f);
            fout.write(result.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Sensor", result);
        super.onDestroy();
    }

}
