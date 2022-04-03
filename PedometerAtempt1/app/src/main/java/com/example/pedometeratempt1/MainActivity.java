package com.example.pedometeratempt1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView StepCounterVw, StepDetectVw;
    private SensorManager sensorManager;
    private Sensor nSensorCounter, nStepDetector;
    private boolean counterAvailability, stepDetectorAvailability;
    int stepcount = 0, stepsDtected = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Conectar el view con el objeto
        StepDetectVw=(TextView)findViewById(R.id.Step_detector);
        StepCounterVw=(TextView)findViewById(R.id.StepCounterTV);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){ //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            nSensorCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            counterAvailability = true;
        }else{
            StepCounterVw.setText("Step counter is not vailable");
            counterAvailability = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            nStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            stepDetectorAvailability = true;
        }
        else{
            StepDetectVw.setText("Step detector is unavailable");
            stepDetectorAvailability = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == nSensorCounter){
            stepcount = (int) sensorEvent.values[0];
            StepCounterVw.setText(String.valueOf(stepcount));
        }else if (sensorEvent.sensor == nStepDetector){
            stepsDtected = (int) (stepsDtected+sensorEvent.values[0]);
            StepDetectVw.setText(String.valueOf(stepsDtected));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!= null){
            sensorManager.registerListener(this, nSensorCounter, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.registerListener(this,nStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!= null){
            sensorManager.unregisterListener(this, nSensorCounter);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.unregisterListener(this, nStepDetector);
        }
    }
}