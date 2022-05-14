package com.example.pedometeratempt1;

import static android.content.ContentValues.TAG;

import static java.lang.Math.round;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.content.SharedPreferences;
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

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView  StepDetectVw, CountDownTV, TotalStepVw;//UI conections
    private String CurDate,CurHour;//Store date date stored as 5/14/22
    int stepsDtected = 0, stepCounter =0;//Counters for UI
    private SensorManager sensorManager;//Sensor obj
    private Sensor  nStepDetector;//Step detector obj
    private boolean  stepDetectorAvailability;//sensor bool
    private static final long START_TIME_IN_MILLIS = 600000;//Amount of time for timer 600000 = 10 min
    private long timeleft=86400000;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;//Store current time left
    private CountDownTimer mCountdowntimer;//CountDown obj
    public static String SHARED_PREFS = "shared_preferences",SAVED_HOUR="Registry_hour",SAVED_STEPS="Total_steps_today",SAVED_DATE="saved_date_info";



    @SuppressLint({"SourceLockedOrientationActivity", "InlinedApi"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //Avoid it from rotating since it restarts the countdown
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Connect with UI
        CountDownTV=(TextView)findViewById(R.id.CountDowntTxt);
        StepDetectVw=(TextView)findViewById(R.id.Step_detector);
        TotalStepVw=(TextView)findViewById(R.id.TotalStepsTV);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//maintain portrait view

        if(ContextCompat.checkSelfPermission(this, //Obtain sensor aproval
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        loadData();

        //run pedometer bool
        pedometerAvailability();
        //Obtain latest info from phone
        updateInfo();
        //Run the countdown
        countDown();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }

    //What to do when the app resumes
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.registerListener(this,nStepDetector, SensorManager.SENSOR_DELAY_UI);
        }
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
        registrarDatos(this.CurDate,this.CurHour,this.stepCounter);
        countDown();
    }

    public void registrarDatos (String date, String hour, int steps){
        DataModel dataModel;
        try{
            updateInfo();
            dataModel = new DataModel(-1, date, hour,steps);
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

    public void saveData(){
        SharedPreferences sharePref = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePref.edit();

        updateInfo();
        editor.putString(SAVED_HOUR,CurHour);

        Integer temp = Integer.valueOf(String.valueOf(TotalStepVw.getText()));
        editor.putInt(SAVED_STEPS,temp);

        editor.putString(SAVED_DATE,CurDate);
    }
    public void loadData(){
        SharedPreferences sharePref = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);

        String tempDate = sharePref.getString(SAVED_DATE,CurDate);
        String tempHour = sharePref.getString(SAVED_HOUR,CurHour);
        String tempSteps = sharePref.getString(SAVED_STEPS,"0");

        String [] data = {tempDate,tempHour,tempSteps};
        CalculateDiff(data);
    }

    public void CalculateDiff(@NonNull String[] data){
        if (data[0]!=CurDate){
            data[2]="0";
        }

        TotalStepVw.setText(data[2]);//Update total amount of steps per day

        //Convert to appropriate format
        double pastHour = textTimeToNum(data[1]);

        double newHour = textTimeToNum(CurHour);

        double difference = newHour - pastHour;

        //More then 10 minutes passed
        if (difference > 0.10){
            accountForLostTime(difference, pastHour, data[0]);
        }


    }
    public double textTimeToNum(@NonNull String data){
        char [] tempTimeText = data.toCharArray();
        double pastHour=0;
        double tempVal;
        for (int i=0; i< tempTimeText.length;i++){
            if(i!=2){

                if(i>2){tempVal= (int) tempTimeText[i] *0.1;}
                else{tempVal= (int) tempTimeText[i];}

            }else{tempVal=0;}

            pastHour = pastHour+tempVal;
        }
        return pastHour;
    }

    public void accountForLostTime(double dif, double oldHour, String lastDate){
        double reps = new BigDecimal(dif).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        reps = Math.toRadians(reps * 6);
        reps = new BigDecimal(reps).setScale(1, BigDecimal.ROUND_DOWN).doubleValue();

        //format of date mm/dd/yy (it can also be m/d/yy)
        char [] date = lastDate.toCharArray();
        int [] nDate= new int[3];
        int counter = 0;
        for (int i =0; i< date.length;i++){

            if (date[i]!='/'){
                nDate[counter]=Integer.valueOf(date[i]);
            }else{
                counter++;
            }
        }

        int hour;
        double minute;
        String tempDate = numberToDate(nDate);//Date values to string

        //cycle to register every lost 10 min registry
        for (int i = 0; i<reps; i++){

            //Separate hour and minute 10.30 => 10 (hour), 0.30(minute)
            hour = (int)oldHour; // 10.30 = 10
            minute = hour - oldHour;// 10.30 - 10 = 0.30

            if (hour>24){//past 12am new day
                hour = 0;
                minute = 0;
                nDate[0]++;
                tempDate = numberToDate(nDate);
            }
            else if (minute >= 0.6){// 60 min = 1hr
                hour++;
                minute = 0.0;
            }

            String temp = String.valueOf(hour)+":"+String.valueOf(minute);

            registrarDatos(tempDate,temp,0);

            oldHour = hour+minute+0.1;
        }

    }
    public String numberToDate(@NonNull int[] nDate){
        String tempDate = "";
        return tempDate= String.valueOf(nDate[0])+'/'+String.valueOf(nDate[1])+'/'+String.valueOf(nDate[2]);
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