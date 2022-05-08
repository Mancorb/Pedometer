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
    private String CurHour;
    private String CurDate;
    private SensorManager sensorManager;
    private Sensor  nStepDetector;
    private boolean  stepDetectorAvailability;
    private static final long START_TIME_IN_MILLIS = 600000;//Ammount of time for timmer 600000 = 10 min
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    public static final String
            SHARED_PREFS ="sharedPrefs",
            STEPS ="Steps taken",
            TIME ="Time left";

    //Check pedometer availability & START timer
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Check pedometer availability
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            nStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            stepDetectorAvailability = true;
        }
        else{stepDetectorAvailability = false;}
        if(!stepDetectorAvailability){
            Toast.makeText(this, "Pedometer Unavailable", Toast.LENGTH_LONG).show();
        }
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
                Log.e(TAG, timeLeftFormated);
                //UPDATE SHARED PREFS
                setSharedTime(timeLeftFormated);
            }
            @Override
            public void onFinish() {
                restartTimer();
            }
        }.start();
    }

    public void restartTimer(){
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        addToDataBase();
        createTimer();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == nStepDetector){
            int newStep=0;
            newStep = (int) (sensorEvent.values[0]);

            //ADD IT TO SHARED PREFS
            if (newStep>0){
                int steps = getSharedSteps();
                setSharedSteps((steps+newStep));
            }
            //broadcast data
        }
    }

    //Add registered data into Database
    public void addToDataBase(){
        DataModel dataModel;
        try{
            updateDateTimeInfo();
            dataModel = new DataModel(-1, this.CurDate, this.CurHour, getSharedSteps());
            //Toast.makeText(this, "Datos registrdos"+dataModel, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            dataModel = new DataModel(-1, "Error", "Error",0);
            Toast.makeText(this, "Error no se registraron los datos", Toast.LENGTH_SHORT).show();
        }

        //Make reference to the database
        AdminSQLiteOpenHelper dataBaseHelper = new AdminSQLiteOpenHelper(StepCounterService.this);

        boolean success = dataBaseHelper.addOne(dataModel);
        //CHAGE SHARED PREFS back to 0
        setSharedSteps(0);
    }

    //Sets the current time from system info
    public void updateDateTimeInfo(){
        Calendar calendar = Calendar.getInstance();
        CurDate = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());
        SimpleDateFormat format  = new SimpleDateFormat("HH:mm");
        CurHour = format.format(calendar.getTime());
        setSharedTime(CurHour);
    }

    //GETTER & SETTER of shared preferences
    public Integer getSharedSteps(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getInt(STEPS, 0);
    }
    public void setSharedSteps(int nSteps){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEPS,nSteps);
    }
    public String getSharedTime(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(TIME, "00:00");
    }
    public void setSharedTime(String nTime){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TIME,nTime);
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
