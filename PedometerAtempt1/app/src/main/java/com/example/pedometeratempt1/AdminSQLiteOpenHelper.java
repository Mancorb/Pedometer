package com.example.pedometeratempt1;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper{

    public static final String TABLA_ACTIVIDAD = "REGISTRO_ACTIVIDAD";
    public static final String COLUMN_FECHA = "FECHA";
    public static final String COLUMN_HORA = "HORA";
    public static final String COLUMN_PASOS = "PASOS";

    public AdminSQLiteOpenHelper(@Nullable Context context) { //constructor de la base de datos
        super(context, "registro.db", null, 1);
    }
    //function called the FIRST TIME the database is accessed. Create the database here
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE "+ TABLA_ACTIVIDAD + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FECHA + " TEXT, " + COLUMN_HORA + " TEXT, " + COLUMN_PASOS + " INTEGER)";

        db.execSQL(createTableStatement);
    }

    //Function called if the version number changes in development
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //Method to add a new item to the DataBase
    public boolean addOne(DataModel dataModel){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_FECHA, dataModel.getFecha());
        cv.put(COLUMN_HORA, dataModel.getHora());
        cv.put(COLUMN_PASOS, dataModel.getPasos());

        long insert = db.insert(TABLA_ACTIVIDAD, null, cv);

        if(insert == 1){
            return false;
        }
        else {
            return false;
        }
    }
}
