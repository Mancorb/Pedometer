package com.example.pedometeratempt1;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    public List<DataModel> getEverything(){
        List<DataModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM "+TABLA_ACTIVIDAD;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString,null);//cursor is the result set

        if(cursor.moveToFirst()){
            //loop through the results
            do{

                int id = cursor.getInt(0);
                String date = cursor.getString(1);
                String time = cursor.getString(2);
                int steps = cursor.getInt(3);

                DataModel newData = new DataModel(id,date,time,steps);

                returnList.add(newData);

            }while(cursor.moveToNext());

        }else{
            //failure to add anithing to the list

        }
        cursor.close();
        db.close();

        return returnList;
    }
}
