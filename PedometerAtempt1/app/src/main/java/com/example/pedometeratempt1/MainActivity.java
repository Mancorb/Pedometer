package com.example.pedometeratempt1;

import static android.content.ContentValues.TAG;

import static java.lang.Math.round;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.DatabaseMetaData;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView  StepDetectVw, CountDownTV, TotalStepVw;//UI conections
    private Button exportB;
    private String CurDate,CurHour,tempDate,tempHour,tempSteps;//Store date date stored as 5/14/22
    int stepsDtected = 0, stepCounter =0;//Counters for UI
    private SensorManager sensorManager;//Sensor obj
    private Sensor  nStepDetector;//Step detector obj
    private boolean  stepDetectorAvailability;//sensor bool
    private static final long START_TIME_IN_MILLIS = 600000;//Amount of time for timer 600000 = 10 min
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;//Store current time left
    private CountDownTimer mCountdowntimer;//CountDown obj
    public static String SHARED_PREFS = "shared_preferences",SAVED_HOUR="Registry_hour",SAVED_STEPS="Total_steps_today",SAVED_DATE="saved_date_info";

    private static final DecimalFormat dfZero = new DecimalFormat("0.00");


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

        //run pedometer bool
        pedometerAvailability();
        //Obtain latest info from phone
        updateInfo();
        //Load saved data
        loadData();
        //Run the countdown
        countDown();

    }

    @Override
    protected void onPause() {
        super.onPause();
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
    //--------------------------------------------------------

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
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        registrarDatos(this.CurDate,this.CurHour,this.stepCounter);
        countDown();
    }

    public void registrarDatos (String date, String hour, int steps){
        DataModel dataModel;
        try{
            updateInfo();
            dataModel = new DataModel(-1, date, hour,steps);
        }
        catch (Exception e){
            dataModel = new DataModel(-1, "Error", "Error",0);
            Toast.makeText(this, "Error no se registraron los datos", Toast.LENGTH_SHORT).show();
        }

        //Make reference to the database
        AdminSQLiteOpenHelper dataBaseHelper = new AdminSQLiteOpenHelper(MainActivity.this);

        dataBaseHelper.addOne(dataModel);
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

    //Obtain cumulative amount of steps
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == nStepDetector){
            stepCounter++;
            stepsDtected = (int) (stepsDtected+sensorEvent.values[0]);
            int temp = Integer.valueOf(TotalStepVw.getText().toString());
            TotalStepVw.setText(String.valueOf(temp+1));
            StepDetectVw.setText(String.valueOf(stepCounter));

        }
    }

    public void saveData(){
        SharedPreferences sharePref = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePref.edit();

        updateInfo();


        String temp = TotalStepVw.getText().toString();
        editor.putString(SAVED_STEPS,temp);
        editor.putString(SAVED_HOUR,CurHour);
        editor.putString(SAVED_DATE,CurDate);
        editor.apply();

        //Toast.makeText(this, "Saved"+CurHour+" - "+temp+" - "+CurDate, Toast.LENGTH_SHORT).show();
    }
    public void loadData(){

        SharedPreferences sharePref = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);

        tempDate = sharePref.getString(SAVED_DATE,CurDate);
        tempHour = sharePref.getString(SAVED_HOUR,CurHour);
        tempSteps = sharePref.getString(SAVED_STEPS,String.valueOf(TotalStepVw.getText()));

        String [] data = {tempDate,tempHour,tempSteps};
        //Toast.makeText(this, "found this stored data:"+data[0]+"-"+data[1]+"-"+data[2], Toast.LENGTH_SHORT).show();
        CalculateDiff(data);
    }

    public void CalculateDiff(@NonNull String[] data){

        TotalStepVw.setText(data[2]);

        //Convert to appropriate format
        double pastHour = textTimeToNum(data[1]);

        double newHour = textTimeToNum(CurHour);

        double difference = newHour - pastHour;
        Log.d(TAG, "CalculateDiff: "+difference);
        //More then 10 minutes passed
        if (difference > 0.1){
            accountForLostTime(difference, pastHour, data[0]);
        }


    }
    public double textTimeToNum(@NonNull String data){
        char [] tempTimeText = data.toCharArray();
        String tempPastHour="";
        double tempVal;
        for (int i=0; i< tempTimeText.length;i++){

            if (tempTimeText[i]==':'){
                tempPastHour=tempPastHour+'.';
            }else {
                tempPastHour = tempPastHour + tempTimeText[i];
            }
        }
        return Double.parseDouble(tempPastHour);
    }

    public void accountForLostTime(double dif, double oldHour, @NonNull String lastDate){
        double reps = round(dif,1);
        reps = (reps * 60)/10;
        reps = round(reps,1);

        //format of date mm/dd/yy (it can also be m/d/yy)
        char [] date = lastDate.toCharArray();
        int [] nDate= new int[3];
        String number = "";
        int counter = 0;
        for (int i =0; i< date.length;i++){

            if (date[i]!='/'){
                char temp = date[i];
                number = number + date[i];

            }else{
                nDate[counter]= Integer.parseInt(number);
                number = "";
                counter++;
            }
        }
        nDate[counter]= Integer.parseInt(number);

        int hour;
        double minute;
        int finalMinute;
        String tempDate = numberToDate(nDate);//Date values to string

        //cycle to register every lost 10 min registry
        for (int i = 0; i<reps; i++){

            //Separate hour and minute 10.30 => 10 (hour), 0.30(minute)
            hour = (int)oldHour; // 10.30 = 10

            minute = round(oldHour - hour,2);// 10.30 - 10 = 0.30

            if (hour>24){//past 12am new day
                hour = 0;
                minute = 0;
                nDate[1]++;
                tempDate = numberToDate(nDate);
                TotalStepVw.setText("0");
            }
            else if (minute >= 0.6){// 60 min = 1hr
                hour++;
                minute = 0.0;
            }

            finalMinute = (int) Math.round(minute*100);
            String temp;
            if (finalMinute<10){
                temp = String.valueOf(hour) + ":0" + String.valueOf(finalMinute);
            }else {
                temp = String.valueOf(hour) + ":" + String.valueOf(finalMinute);
            }
            registrarDatos(tempDate,temp,0);


            oldHour = hour+minute+0.1;
        }
    }
    public String numberToDate(@NonNull int[] nDate){
        String tempDate = "";
        return tempDate= String.valueOf(nDate[0])+'/'+String.valueOf(nDate[1])+'/'+String.valueOf(nDate[2]);
    }

    //Export data
    public void exportData(View view){
        AdminSQLiteOpenHelper databaseHelper = new AdminSQLiteOpenHelper(MainActivity.this);
        List<DataModel> data = databaseHelper.getEverything();

        for (int i = 0; i< data.size(); i++){
            DataModel temp = data.get(i);
            String res = temp.getFecha()+' '+temp.getHora()+' '+temp.getPasos();


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


    public static double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

}