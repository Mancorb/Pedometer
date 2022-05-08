package com.example.pedometeratempt1;

import static android.content.ContentValues.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView  StepDetectVw, CountDownTV, TotalStepVw;//UI conections
    private String CurDate,CurHour;//Store date
    int stepsDtected = 0, stepCounter =0;//Counters for UI
    private SensorManager sensorManager;//Sensor obj
    private Sensor  nStepDetector;//Step detector obj
    private boolean  stepDetectorAvailability;//sensor bool
    private static final long START_TIME_IN_MILLIS = 600000;//Amount of time for timer 600000 = 10 min
    private long timeleft=86400000;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;//Store current time left
    private CountDownTimer mCountdowntimer;//CountDown obj
    public TextView  StepDetectVw, CountDownTV, TotalStepVw;


    @SuppressLint({"SourceLockedOrientationActivity", "InlinedApi"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Avoid it from rotating since it restarts the countdown
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Conect with UI
        CountDownTV=(TextView)findViewById(R.id.CountDowntTxt);
        StepDetectVw=(TextView)findViewById(R.id.Step_detector);
        TotalStepVw=(TextView)findViewById(R.id.TotalStepsTV);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//maintain portrait view

        if(ContextCompat.checkSelfPermission(this, //Obtain sensor aproval
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
        //run pedometer bool
        pedometerAvailability();
        //Obtain latest info from phone
        updateInfo();
        //Run the countdown
        stayAliveCountDown();
        countDown();

    }




    //What to do when the app resumes
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.registerListener(this,nStepDetector, SensorManager.SENSOR_DELAY_UI);
        }
    }


    public void stayAliveCountDown (){
        //countdown 86,400,000

        mCountdowntimer = new CountDownTimer(timeleft, 1000) {

            @Override
            public void onTick(long millsUntilFinished) {
                timeleft = millsUntilFinished;
            }

            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, "sucess", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    // Uses certain ammount of miliseconds and counts down (it reflects the equivalent time in screen as minutes and seconds)
    public void countDown (){
        //countdown
        mCountdowntimer = new CountDownTimer(mTimeLeftInMillis, 1000) {

            @Override
            public void onTick(long millsUntilFinished) {
                mTimeLeftInMillis = millsUntilFinished;
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

    //Restart the count down with original ammount of miliseconds as well as call again the timmer method
    public void timeFinnished (){
        //Toast.makeText(this, "Time is up", Toast.LENGTH_SHORT).show();
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        registrarDatos();
        countDown();
    }

    public void registrarDatos (){
        DataModel dataModel;
        try{
            updateInfo();
            dataModel = new DataModel(-1, this.CurDate, this.CurHour,this.stepCounter);
            //Toast.makeText(this, "Datos registrdos"+dataModel, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            dataModel = new DataModel(-1, "Error", "Error",0);
            Toast.makeText(this, "Error no se registraron los datos", Toast.LENGTH_SHORT).show();
        }

        //Make reference to the database
        AdminSQLiteOpenHelper dataBaseHelper = new AdminSQLiteOpenHelper(MainActivity.this);

        boolean success = dataBaseHelper.addOne(dataModel);
        StepDetectVw.setText("0");
        stepCounter = 0;
    }

    public void updateInfo(){
        //Obtain system date
        Calendar calendar = Calendar.getInstance();
        this.CurDate = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());
        SimpleDateFormat format  = new SimpleDateFormat("HH:mm");
        this.CurHour = format.format(calendar.getTime());
    }

    //Obtain cumulative ammount of steps
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == nStepDetector){
            stepCounter++;
            stepsDtected = (int) (stepsDtected+sensorEvent.values[0]);
            TotalStepVw.setText(String.valueOf(stepsDtected));
            StepDetectVw.setText(String.valueOf(stepCounter));

        }
    }


    //---------------------------------------

    public void pedometerAvailability(){
        //Change bool (true = pedometer available)
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            nStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            stepDetectorAvailability = true;
        }
        else{
            StepDetectVw.setText("No se puede acceder al sensor");
            stepDetectorAvailability = false;
        }
    }
    //what to do when the accuracy changes (Obligatory)

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}