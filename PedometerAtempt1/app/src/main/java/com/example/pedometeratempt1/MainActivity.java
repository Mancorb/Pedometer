package com.example.pedometeratempt1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final long START_TIME_IN_MILLIS = 6000;
    private long mTimeLeftInMillis=START_TIME_IN_MILLIS;
    private CountDownTimer mCountdowntimer;

    private String CurDate;

    private TextView  StepDetectVw, CountDownTV;
    private SensorManager sensorManager;
    private Sensor  nStepDetector;
    private Button showData;
    private boolean  stepDetectorAvailability;
    int stepsDtected = 0, stepCounter =0;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //----------------------------------------------------------------
        //Avoid it from rotating since it restarts the countdown
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //----------------------------------------------------------------
        //Obtain system date
        Calendar calendar = Calendar.getInstance();
        CurDate = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());
        //----------------------------------------------------------------


        //Conect with UI
        CountDownTV=(TextView)findViewById(R.id.CountDowntTxt);
        StepDetectVw=(TextView)findViewById(R.id.Step_detector);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        showData = (Button) findViewById(R.id.AllDataB);

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
        //----------------------------------------------------------------

        //Run the countdown
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
    //what to do when the accuracy changes (Obligatory)
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //What to do when the app resumes
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.registerListener(this,nStepDetector, SensorManager.SENSOR_DELAY_UI);
        }
    }

    //--------------------------------------
    //Restart the count down with original ammount of miliseconds as well as call again the timmer method
    public void timeFinnished (){
        Toast.makeText(this, "Time is up", Toast.LENGTH_SHORT).show();
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        registrarDatos();
        countDown();
    }
    // Uses certain ammount of miliseconds and counts down (it reflects the equivalent time in screen as minutes and seconds)
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
    //------------------------------------
    public void registrarDatos (){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "RegistroPasos", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();
        Cursor fila = BaseDeDatos.rawQuery("SELECT MAX(id) FROM RegistroPasos", null);

        if(fila.moveToFirst()) {
            ContentValues registroDatos = new ContentValues();
            registroDatos.put("id", fila.getString(0));
            registroDatos.put("fecha", CurDate);
            registroDatos.put("pasos", stepCounter);

            BaseDeDatos.insert("RegistroPasos", null, registroDatos);
            BaseDeDatos.close();

            Toast.makeText(this, "Datos pasos registrados", Toast.LENGTH_SHORT).show();

            stepCounter=0;
        }
    }
    //Cambio de pantalla
    public void AbrirRegistros (View view){
        //Intent siguiente = new Intent(this, SegundoActi)
    }
}