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
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final long START_TIME_IN_MILLIS = 100000;
    private long mTimeLeftInMillis=START_TIME_IN_MILLIS;
    private CountDownTimer mCountdowntimer;

    private TextView  StepDetectVw, CountDownTV;
    private SensorManager sensorManager;
    private Sensor  nStepDetector;
    private boolean  stepDetectorAvailability;
    int stepsDtected = 0, stepCounter =0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Conect with UI
        CountDownTV=(TextView)findViewById(R.id.CountDowntTxt);
        StepDetectVw=(TextView)findViewById(R.id.Step_detector);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Ask for permission to access pedometer sensor
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
        //Stop phone from sleeping
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Change bool (true = pedometer available)
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            nStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            stepDetectorAvailability = true;
        }
        else{
            StepDetectVw.setText("No se puede acceder al sensor");
            stepDetectorAvailability = false;
        }
        countDown();

    }
    //Obtain cumulative ammount of steps
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == nStepDetector){
            stepsDtected = (int) (stepsDtected+sensorEvent.values[0]);
            StepDetectVw.setText(String.valueOf(stepsDtected));
            stepCounter++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //What to do when the app resumes
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.registerListener(this,nStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    //What to do when the app in no longer in main vew yet still running
    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.unregisterListener(this, nStepDetector);
        }
    }

    public void timeFinnished (){
        Toast.makeText(this, "Time is up", Toast.LENGTH_SHORT).show();
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        countDown();
    }
    public void countDown (){
        //countdown
        mCountdowntimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millsUntilFinnished) {
                mTimeLeftInMillis = millsUntilFinnished;
                int minutes = (int) (mTimeLeftInMillis/1000) /60;
                int seconds = (int) (mTimeLeftInMillis/1000) %60;
                String timeLeftFormated = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                CountDownTV.setText(timeLeftFormated);
            }

            @Override
            public void onFinish() {
                timeFinnished();
            }
        }.start();
    }
}