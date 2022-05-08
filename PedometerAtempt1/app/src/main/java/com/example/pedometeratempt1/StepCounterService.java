package com.example.pedometeratempt1;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private String timeLeftFormated ="";
    private SensorManager sensorManager;
    private Sensor  nStepDetector;
    private boolean  stepDetectorAvailability;
    private static final long START_TIME_IN_MILLIS = 600000;//Ammount of time for timmer 600000 = 10 min
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    //Check pedometer availability & START timer
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Check pedometer availability
        nStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //Start Timer
        createTimer();

        return START_NOT_STICKY;
    }


    public void createTimer (){
        CountDownTimer mCountdowntimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millsUntilFinnished) {
                mTimeLeftInMillis = millsUntilFinnished;
                int minutes = (int) (mTimeLeftInMillis/1000) /60;
                int seconds = (int) (mTimeLeftInMillis/1000) %60;
                timeLeftFormated = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

            }
            @Override
            public void onFinish() {
                restartTimer();
            }
        }.start();
    }

    public void restartTimer(){
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        Log.d(TAG, "Time restarted");
        createTimer();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == nStepDetector){
            int newStep=0;
            newStep = (int) (sensorEvent.values[0]);

            //ADD IT TO SHARED PREFS
            if (newStep>0){
                Log.d(TAG, "onSensorChanged: step detected");
            }
            //broadcast data
        }
    }



    //UNECESARY YET OBLIGATORY FOR FUNCTIONALITY
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
