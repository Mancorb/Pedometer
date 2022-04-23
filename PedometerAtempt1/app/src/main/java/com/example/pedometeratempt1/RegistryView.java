package com.example.pedometeratempt1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;

public class RegistryView extends AppCompatActivity {

    private ListView vistaRegistros;

    private String pasos [] = new String[100];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry_view);

        vistaRegistros=(ListView)findViewById(R.id.ListOfData);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "RegistroPasos", null,1);
        SQLiteDatabase BaseDeDatos = admin.getReadableDatabase();
        Cursor fila = BaseDeDatos.rawQuery("SELECT id FROM RegistroPasos", null);

        for (int i = 1; i<fila.getCount(); i++){
            pasos[i]=fila.getString(i);
        }

        ArrayAdapter <String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_data_base, pasos);
        vistaRegistros.setAdapter(adapter);
    }

    public void returnMain(View view){
        Intent anterior = new Intent(this, MainActivity.class);
        startActivity(anterior);
    }

}