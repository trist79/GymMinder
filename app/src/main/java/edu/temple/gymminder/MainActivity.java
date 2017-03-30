package edu.temple.gymminder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private TextView accelX, accelY, accelZ, rotX, rotY, rotZ;
    Button button;
    private String stopRecordingMessage = "Stop Recording";
    private String startRecordingMessage = "Start Recording";

    private boolean isRecording = false;
    float mAccelX = 0, mAccelY = 0, mAccelZ = 0, mRotX = 0, mRotY = 0, mRotZ = 0;
    private String accelXResult = "", accelYResult = "", accelZResult = "", rotXResult = "", rotYResult = "", rotZResult = "", timestamp = "";

    StorageReference storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance().getReference();
        accelX = (TextView) findViewById(R.id.accelX);
        accelY = (TextView) findViewById(R.id.accelY);
        accelZ = (TextView) findViewById(R.id.accelZ);
        rotX = (TextView) findViewById(R.id.rotX);
        rotY = (TextView) findViewById(R.id.rotY);
        rotZ = (TextView) findViewById(R.id.rotZ);

        button = (Button) findViewById(R.id.button);
        button.setText(startRecordingMessage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                if (isRecording) {
                    startRecording();
                    button.setText(stopRecordingMessage);
                } else {
                    stopRecording();
                    button.setText(startRecordingMessage);
                }
            }
        });

    }

    private void onAccelerometerChanged(SensorEvent event) {
        float x = Math.abs(event.values[0]) > 0.09 ? event.values[0] : 0;
        float y = Math.abs(event.values[1]) > 0.09 ? event.values[1] : 0;
        float z = Math.abs(event.values[2]) > 0.09 ? event.values[2] : 0;
        mAccelX = Math.abs(mAccelX) > Math.abs(x) ? mAccelX : x;
        mAccelY = Math.abs(mAccelY) > Math.abs(y) ? mAccelY : y;
        mAccelZ = Math.abs(mAccelZ) > Math.abs(z) ? mAccelZ : z;
        accelX.setText(String.valueOf(x));
        accelY.setText(String.valueOf(y));
        accelZ.setText(String.valueOf(z));
        accelXResult+=x + ",";
        accelYResult+=y + ",";
        accelZResult+=z + ",";
        timestamp += event.timestamp + ",";
    }

    private void onGyroscopeChanged(SensorEvent event) {
        float x = Math.abs(event.values[0]) > 0.09 ? event.values[0] : 0;
        float y = Math.abs(event.values[1]) > 0.09 ? event.values[1] : 0;
        float z = Math.abs(event.values[2]) > 0.09 ? event.values[2] : 0;
        mRotX = Math.abs(mRotX) > Math.abs(x) ? mRotX : x;
        mRotY = Math.abs(mRotY) > Math.abs(y) ? mRotY : y;
        mRotZ = Math.abs(mRotZ) > Math.abs(z) ? mRotZ : z;
        rotX.setText(String.valueOf(x));
        rotY.setText(String.valueOf(y));
        rotZ.setText(String.valueOf(z));
        rotXResult+=x + ",";
        rotYResult+=y + ",";
        rotZResult+=z + ",";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            onAccelerometerChanged(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            onGyroscopeChanged(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startRecording() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        } else {
            // no accelerometer!
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
        } else {
            // no gyroscope!
        }
    }

    private void stopRecording() {
        if(accelZResult.length()>0) {
            String accelResult = accelXResult + "\n" + accelYResult + "\n" + accelZResult + "\n" + timestamp;
            String rotResult = rotXResult + "\n" + rotYResult + "\n" + rotZResult;
            File accelFile = new File(getFilesDir(), "accel_test.csv");
            File rotFile = new File(getFilesDir(), "rot_test.csv");
            try {
                FileOutputStream accelOut = new FileOutputStream(accelFile);
                FileOutputStream rotOut = new FileOutputStream(rotFile);
                accelOut.write(accelResult.getBytes());
                rotOut.write(rotResult.getBytes());
                rotOut.close();
                accelOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("LA_Sensor", accelResult);
            Log.d("RV_Sensor", rotResult);
            String s = "_";
            s += Calendar.getInstance().get(Calendar.SECOND) + "_";
            s += Calendar.getInstance().get(Calendar.MINUTE) + "_";
            s += Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            storage.child("repetition_time_series" + s + ".csv").putFile(Uri.fromFile(accelFile)
            ).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        accelXResult="";accelYResult="";accelZResult="";rotXResult="";rotYResult="";rotZResult="";
        timestamp="";
        sensorManager.unregisterListener(this);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        stopRecording();
        isRecording = false;
        button.setText(startRecordingMessage);
    }

    @Override
    protected void onDestroy(){
        stopRecording();
        super.onDestroy();
    }

}
